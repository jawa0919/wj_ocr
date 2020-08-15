import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wj_ocr/wj_ocr.dart';

void main() {
  const MethodChannel channel = MethodChannel('wj_ocr');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await WjOcr.platformVersion, '42');
  });
}
