package top.wiz.wj_ocr

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** WjOcrPlugin */
class WjOcrPlugin : FlutterPlugin, MethodCallHandler {

    val TAG = "WjOcrPlugin"

    private lateinit var context: Context
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "wj_ocr")
        channel.setMethodCallHandler(this);
    }

    companion object {
        lateinit var ocrResult: Result

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "wj_ocr")
            val wjOcrPlugin = WjOcrPlugin()
            wjOcrPlugin.context = registrar.context()
            channel.setMethodCallHandler(wjOcrPlugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "startTessOcr") {
            val language: String = call.argument("language") ?: "eng"
            Log.d(TAG, "onMethodCall: language$language")
            ocrResult = result;
            val intent = Intent(context, TessActivity::class.java)
            intent.putExtra("language", language)
            context.startActivity(intent)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
