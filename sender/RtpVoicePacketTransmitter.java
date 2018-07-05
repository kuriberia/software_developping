package sender;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;

public class RtpVoicePacketTransmitter
{
    private DatagramSocket socket;			// Socket
    private String destIP;					// 宛先IPアドレス
    private String destPort;				// 宛先UDPポート番号
    private TransmitThread transmitThread;	// 送信スレッド

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //	コンストラクタ
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * マイクから入力された音声データを、RTPパケットに加工し、
     * 指定したソケットを使用して、指定した宛先IPアドレス、指定した宛先UDPポート番号
     * へ送信するRTPパケット送信機を生成します
     * ディフォルトのメディアタイプは、G.711 u-law(0)を使用します
     *
     * @param socket 使用するSocket
     * @param destIP 宛先IPアドレス
     * @param destPort 宛先UDPポート番号
     */
    public RtpVoicePacketTransmitter(DatagramSocket socket,String destIP,String destPort)
    {
        this.socket = socket;
        this.destIP = destIP;
        this.destPort = destPort;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //	パブリックメソッド
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * マイクからキャプチャーを開始し、相手へRTPパケットを送信し始めます
     * このメソッドのスレッドはブロックせずにすぐに制御を返します
     * キャプチャー・送信はstop()メソッドが呼ばれると停止します
     */
    public void start()
    {
        this.transmitThread = new TransmitThread(this.socket,this.destIP,this.destPort);
        this.transmitThread.start();
    }

    /**
     * マイクからのキャプチャーを停止し、相手へのRTPパケットの送信を終えます
     */
    public void stop()
    {
        this.transmitThread.transmitStop();
    }

    /**
     * 使用するメディアタイプを変更します
     * ※このメソッドはまだ実装されていません
     */
    public void setMediaType(int mediaType)
    {

    }
}

/**
 * キャプチャー -> 送信スレッド
 */
class TransmitThread extends Thread
{
    private short sequenceNum;		// シーケンス番号
    private int timeStamp;			// タイムスタンプ
    private int syncSourceId;		// 同期ソースID
    private byte marker;			// マーカービット

    private String destIP;
    private String destPort;

    private DatagramSocket socket;
    private boolean isStop;

    // コンストラクタ
    public TransmitThread(DatagramSocket socket,String destIP,String destPort)
    {
        this.socket = socket;
        this.destIP = destIP;
        this.destPort = destPort;
        this.isStop = false;

        // Init RTP Headerstop
        Random r = new Random();
        this.sequenceNum = 0;
        this.timeStamp = 0;
        this.syncSourceId = r.nextInt();
        this.marker = -128;
    }

    // マイクキャプチャー -> 送信 スレッド開始
    public void run()
    {
        try
        {
            byte[] voicePacket = new byte[160];
            byte[] rtpPacket = new byte[172];
            InetSocketAddress address = new InetSocketAddress(this.destIP,Integer.parseInt(this.destPort));
            DatagramPacket packet = null;

            AudioFormat linearFormat = new AudioFormat(8000,16,1,true,false);
            AudioFormat ulawFormat = new AudioFormat(AudioFormat.Encoding.ULAW,8000,8,1,1,8000,false);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class,linearFormat);
            TargetDataLine targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
            targetDataLine.open(linearFormat);
            targetDataLine.start();

            AudioInputStream linearStream = new AudioInputStream(targetDataLine);
            // リニアPCM 16bit 8000Hz から　G.711 u-lawへ変換
            AudioInputStream ulawStream = AudioSystem.getAudioInputStream(ulawFormat,linearStream);

            while(!isStop)
            {
                try
                {
                    // G.711 u-law 20ms分を取得する
                    ulawStream.read(voicePacket,0,voicePacket.length);
                    // RTPヘッダーを付ける
                    rtpPacket = this.addRtpHeader(voicePacket);
                    packet = new DatagramPacket(rtpPacket,rtpPacket.length,address);
                    // 相手へ送信
                    this.socket.send(packet);
                }
                catch(Exception ee)
                {
                    ee.printStackTrace();
                }
            }
            targetDataLine.stop();
            targetDataLine.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    // マイクキャプチャー -> 送信開始 スレッド停止
    public void transmitStop()
    {
        this.isStop = true;
    }

    /**
     * 音声パケットにRTPヘッダーを追加し、できあがったRTPパケットを返します
     * @param voiceData 音声データ
     * @return rtpPacket RTPパケット
     */
    private byte[] addRtpHeader(byte[] voiceData)
    {
        byte[] rtpHeader = new byte[12];	// RTPヘッダ
        byte version	= -128;				// バージョン番号10000000
        byte padding	= 0;				// パディング
        byte extention	= 0;				// 拡張ビット
        byte contribute	= 0;				// コントリビュートカウント
        byte payload	= 0;				// ペイロードタイプ

        // RTPヘッダーの生成
        rtpHeader[0]  = (byte)(version | padding | extention | contribute);
        rtpHeader[1]  = (byte)(marker | payload);
        rtpHeader[2]  = (byte)(this.sequenceNum >> 8);
        rtpHeader[3]  = (byte)(this.sequenceNum >> 0);
        rtpHeader[4]  = (byte)(this.timeStamp >> 24);
        rtpHeader[5]  = (byte)(this.timeStamp >> 16);
        rtpHeader[6]  = (byte)(this.timeStamp >>  8);
        rtpHeader[7]  = (byte)(this.timeStamp >>  0);
        rtpHeader[8]  = (byte)(this.syncSourceId >> 24);
        rtpHeader[9]  = (byte)(this.syncSourceId >> 16);
        rtpHeader[10] = (byte)(this.syncSourceId >>  8);
        rtpHeader[11] = (byte)(this.syncSourceId >>  0);

        // シーケンス番号、タイムスタンプ、マーカービット移行
        this.sequenceNum ++;
        this.timeStamp += 160;
        if(this.marker == -128)
            this.marker = 0;

        // RTPヘッダー＋音声データ = RTPパケット
        ByteArrayOutputStream out = new ByteArrayOutputStream(172);
        out.write(rtpHeader,0,12);
        out.write(voiceData,0,160);

        return out.toByteArray();
    }
}

