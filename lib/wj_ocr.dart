
import 'dart:async';

import 'package:flutter/services.dart';

class WjOcr {
  static const MethodChannel _channel =
      const MethodChannel('wj_ocr');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
