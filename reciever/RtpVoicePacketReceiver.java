package reciever;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class RtpVoicePacketReceiver
{
    private DatagramSocket socket;
    private RecvThread recvThread;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //	コンストラクタ
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 指定したSocketを使用して、RTPパケットを待ち受け、
     * スピーカから再生するRTPパケット受信機を生成します
     *
     * @param socket RTPパケットを待ち受けるSocket
     */
    public RtpVoicePacketReceiver(DatagramSocket socket)
    {
        this.socket = socket;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //	パブリックメソッド
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * RTPパケットの受信を開始し、スピーカーから再生を開始します
     * このメソッドのスレッドはブロックせずにすぐに制御を返します
     * 受信・再生はstop()メソッドが呼ばれると停止します
     */
    public void start()
    {
        this.recvThread = new RecvThread(this.socket);
        this.recvThread.start();
    }

    /**
     * RTPパケットの受信を停止し、スピーカーからの再生を終えます
     */
    public void stop()
    {
        this.recvThread.recvStop();
    }
}

/**
 * 受信 -> 再生スレッド
 */
class RecvThread extends Thread
{
    private DatagramSocket socket;
    private boolean isStop;

    // コンストラクタ
    public RecvThread(DatagramSocket socket)
    {
        this.socket = socket;
        this.isStop = false;
    }

    // 受信 -> 再生 スレッド開始
    public void run()
    {
        try
        {
            byte[] buffer = new byte[172];
            byte[] linearBuffer = new byte[320];

            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

            AudioFormat ulawFormat = new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);
            AudioFormat linearFormat = new AudioFormat(8000,16,1,true,false);
            AudioInputStream ulawStream = null;
            AudioInputStream linearStream = null;

            ByteArrayInputStream byteArrayInputStream = null;

            DataLine.Info info = new DataLine.Info(SourceDataLine.class,linearFormat);
            SourceDataLine sourceDataLine = (SourceDataLine)AudioSystem.getLine(info);
            sourceDataLine.open(linearFormat);
            sourceDataLine.start();

            while(!isStop)
            {
                try
                {
                    // 受信
                    this.socket.receive(packet);

                    // RTPヘッダーを読み飛ばす
                    byteArrayInputStream = new ByteArrayInputStream(packet.getData(),12,160);

                    ulawStream = new AudioInputStream(byteArrayInputStream,ulawFormat,160);
                    // G.711 u-law からリニアPCM 16bit 8000Hzへ変換
                    linearStream = AudioSystem.getAudioInputStream(linearFormat,ulawStream);
                    linearStream.read(linearBuffer,0,linearBuffer.length);

                    // 再生
                    sourceDataLine.write(linearBuffer,0,linearBuffer.length);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            sourceDataLine.stop();
            sourceDataLine.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // 受信 -> 再生 スレッド停止
    public void recvStop()
    {
        this.isStop = true;
    }
}
