import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class WjOcr {
  static const MethodChannel _channel = const MethodChannel('wj_ocr');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// lang: 英文："eng" 简体中文："chi_sim",  默认 "eng"
  static Future<String> startTessOcr({String language = 'eng'}) async {
    if (Platform.isAndroid) {
      String ocrCode = await _channel.invokeMethod<String>(
        'startTessOcr',
        <String, dynamic>{
          "language": language,
        },
      );
      return ocrCode.trim();
    } else {
      throw ("plugin only use android");
    }
  }
}
