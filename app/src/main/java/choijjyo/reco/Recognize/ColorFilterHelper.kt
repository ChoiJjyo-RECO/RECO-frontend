package choijjyo.reco.Recognize

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat

class ColorFilterHelper {

    private val matrixDeuteranopia = arrayOf(
        doubleArrayOf(0.625, 0.375, 0.0),
        doubleArrayOf(0.7, 0.3, 0.0),
        doubleArrayOf(0.0, 0.3, 0.7)
    )

    private val matrixProtanopia = arrayOf(
        doubleArrayOf(0.567, 0.433, 0.0),
        doubleArrayOf(0.558, 0.442, 0.0),
        doubleArrayOf(0.0, 0.242, 0.758)
    )

    private val matrixTritanopia = arrayOf(
        doubleArrayOf(0.95, 0.05, 0.0),
        doubleArrayOf(0.0, 0.433, 0.567),
        doubleArrayOf(0.0, 0.475, 0.525)
    )

    fun applyDeuteranopia(inputBitmap: Bitmap): Bitmap {
        return applyColorBlindnessCorrection(inputBitmap, matrixDeuteranopia)
    }

    fun applyProtanopia(inputBitmap: Bitmap): Bitmap {
        return applyColorBlindnessCorrection(inputBitmap, matrixProtanopia)
    }

    fun applyTritanopia(inputBitmap: Bitmap): Bitmap {
        return applyColorBlindnessCorrection(inputBitmap, matrixTritanopia)
    }


    private fun applyColorBlindnessCorrection(inputBitmap: Bitmap, matrix: Array<DoubleArray>): Bitmap {
        val inputMat = Mat(inputBitmap.width, inputBitmap.height, CvType.CV_8UC4)
        Utils.bitmapToMat(inputBitmap, inputMat)

        val correctedMat = Mat(inputBitmap.width, inputBitmap.height, CvType.CV_8UC4)

        val correctionMatrix = Mat(3, 4, CvType.CV_64F)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                correctionMatrix.put(i, j, matrix[i][j])
            }
        }

        // Set the last column to 0
        for (i in 0 until 3) {
            correctionMatrix.put(i, 3, 0.0)
        }

        Core.transform(inputMat, correctedMat, correctionMatrix)

        val correctedBitmap = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(correctedMat, correctedBitmap)

        return correctedBitmap
    }
}