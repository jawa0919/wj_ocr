package top.wiz.wj_ocr

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** WjOcrPlugin */
class WjOcrPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var activity: Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "wj_ocr")
        channel.setMethodCallHandler(this)
    }

    companion object {
        val TAG = "WjOcrPlugin"
        lateinit var ocrResult: Result

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "wj_ocr")
            val wjOcrPlugin = WjOcrPlugin()
            wjOcrPlugin.activity = registrar.activity()
            channel.setMethodCallHandler(wjOcrPlugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "startTessOcr") {
            val language: String = call.argument("language") ?: "eng"
            Log.d(TAG, "onMethodCall: language：$language")
            ocrResult = result
            val intent = Intent(activity, TessActivity::class.java)
            intent.putExtra("language", language)
            activity.startActivity(intent)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPluginBinding) {
        Log.d(TAG, "onDetachedFromEngine: ")
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        Log.d(TAG, "onDetachedFromActivity: ")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges: ")
        onAttachedToActivity(binding)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.d(TAG, "onAttachedToActivity: ")
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d(TAG, "onDetachedFromActivityForConfigChanges: ")
        onDetachedFromActivity()
    }
}
