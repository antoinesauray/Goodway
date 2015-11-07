package io.goodway.model.bonus;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by antoine on 11/7/15.
 */
public class QrCode {

    public static Bitmap generate(String data)throws WriterException {
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata = Uri.encode(data, "utf-8");

        int size = 500;
        BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE,size, size);
        Bitmap ret = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < size; i++) {//width
            for (int j = 0; j < size; j++) {//height
                ret.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
            }
        }
        return ret;
    }
}
