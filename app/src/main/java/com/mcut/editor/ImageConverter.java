package com.mcut.editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageConverter {

    private static final String MIME_TYPE = "video/avc"; // H.264 Video Format
    private static final int FRAME_RATE = 30; // 30 fps
    private static final int IFRAME_INTERVAL = 1; // I-Frame ทุกๆ 1 วินาที
    private static final int VIDEO_DURATION_MS = 3000; // 3 วินาที

    /**
     * แปลง Bitmap ให้เป็นไฟล์วิดีโอชั่วคราวความยาว 3 วินาที
     */
    public static String convertImageToVideo(Bitmap bitmap, File outputDir) throws IOException {
        File outputFile = new File(outputDir, "img_vid_" + System.currentTimeMillis() + ".mp4");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // ปรับความละเอียดให้หาร 16 ลงตัว (ข้อกำหนดของ MediaCodec)
        width = (width + 15) & ~15;
        height = (height + 15) & ~15;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000); // 2 Mbps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        MediaCodec codec = MediaCodec.createEncoderByType(MIME_TYPE);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surface = codec.createInputSurface();
        codec.start();

        // ใช้ 0 แทนค่าคงที่เพื่อความเข้ากันได้ทุกเวอร์ชันของ Android (0 = MUXER_OUTPUT_FORMAT_MPEG_4)
        MediaMuxer muxer = new MediaMuxer(outputFile.getAbsolutePath(), 0);
        int videoTrackIndex = -1;
        boolean muxerStarted = false;

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int totalFrames = (FRAME_RATE * VIDEO_DURATION_MS) / 1000; // 90 เฟรม สำหรับ 3 วินาที

        for (int i = 0; i < totalFrames; i++) {
            // 1. วาด Bitmap ลงบน Surface ของ Encoder
            Canvas canvas = surface.lockCanvas(null);
            try {
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(scaledBitmap, 0, 0, null);
            } finally {
                surface.unlockCanvasAndPost(canvas);
            }

            // 2. ดึงข้อมูลที่เข้ารหัสแล้วออกจาก Codec
            drainEncoder(codec, muxer, bufferInfo, false, videoTrackIndex, muxerStarted);
            if (!muxerStarted) {
                // แก้ไขเป็น getOutputFormat() ตามมาตรฐาน Java/Android
                MediaFormat newFormat = codec.getOutputFormat();
                videoTrackIndex = muxer.addTrack(newFormat);
                muxer.start();
                muxerStarted = true;
            }
        }

        // ส่งสัญญาณบอกจุดสิ้นสุดสตรีม (EOS)
        codec.signalEndOfInputStream();
        drainEncoder(codec, muxer, bufferInfo, true, videoTrackIndex, muxerStarted);

        // ปิดการทำงานและคืนทรัพยากร
        codec.stop();
        codec.release();
        muxer.stop();
        muxer.release();

        return outputFile.getAbsolutePath();
    }

    private static void drainEncoder(MediaCodec codec, MediaMuxer muxer, MediaCodec.BufferInfo bufferInfo, boolean endOfStream, int trackIndex, boolean muxerStarted) {
        if (endOfStream) {
            try {
                codec.signalEndOfInputStream();
            } catch (Exception ignored) {}
        }

        while (true) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break;
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxerStarted) {
                    throw new RuntimeException("Format changed twice");
                }
            } else if (outputBufferId < 0) {
                // ข้ามข้อมูลสถานะอื่นๆ
            } else {
                ByteBuffer encodedData = codec.getOutputBuffer(outputBufferId);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + outputBufferId + " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0 && muxerStarted) {
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                }

                codec.releaseOutputBuffer(outputBufferId, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }
}
