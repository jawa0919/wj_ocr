/*
 * @FilePath     : \flutter_wiz_app\lib\util\permission_util.dart
 * @Date         : 2020-08-05 21:10:51
 * @Author       : wangjia(jawa0919@163.com)
 * @Description  : 权限工具
 */

import 'dart:io';

import 'package:permission_handler/permission_handler.dart';

class PermissionUtil {
  /// 启动时请求权限
  static Future<String> launchPermissionRequest() async {
    Map<Permission, PermissionStatus> statuses = await [
      Permission.notification,
      Permission.storage,
      if (Platform.isIOS) Permission.photos,
    ].request();
    return statuses.toString();
  }

  static Future<bool> location() async {
    if (await Permission.location.serviceStatus != ServiceStatus.enabled) {
      throw ("位置服务未打开");
    }
    await Permission.location.request();
    if (await Permission.location.isGranted) {
      return true;
    } else if (await Permission.location.isDenied) {
      await Permission.location.request();
      if (await Permission.location.isGranted) {
        return true;
      } else {
        throw ("您取消了位置授权");
      }
    } else if (await Permission.locationWhenInUse.isPermanentlyDenied) {
      throw ("您拒绝了位置授权，请在设置中打开");
    } else {
      throw ("位置授权未知错误");
    }
  }

  static Future<bool> camera() async {
    await Permission.camera.request();
    if (await Permission.camera.isGranted) {
      return true;
    } else if (await Permission.camera.isDenied) {
      await Permission.camera.request();
      if (await Permission.camera.isGranted) {
        return true;
      } else {
        throw ("您取消了相机授权");
      }
    } else if (await Permission.camera.isPermanentlyDenied) {
      throw ("您拒绝了相机授权，请在设置中打开");
    } else {
      throw ("相机授权未知错误");
    }
  }

  static Future<bool> microphone() async {
    await Permission.microphone.request();
    if (await Permission.microphone.isGranted) {
      return true;
    } else if (await Permission.microphone.isDenied) {
      await Permission.microphone.request();
      if (await Permission.microphone.isGranted) {
        return true;
      } else {
        throw ("您取消了麦克风授权");
      }
    } else if (await Permission.microphone.isPermanentlyDenied) {
      throw ("您拒绝了麦克风授权，请在设置中打开");
    } else {
      throw ("麦克风授权未知错误");
    }
  }

  static Future<bool> storage() async {
    await Permission.storage.request();
    if (await Permission.storage.isGranted) {
      return true;
    } else if (await Permission.storage.isDenied) {
      await Permission.storage.request();
      if (await Permission.storage.isGranted) {
        return true;
      } else {
        throw ("您取消了文件存储授权");
      }
    } else if (await Permission.storage.isPermanentlyDenied) {
      throw ("您拒绝了文件存储授权，请在设置中打开");
    } else {
      throw ("文件存储授权未知错误");
    }
  }

  static Future<bool> photos() async {
    await Permission.photos.request();
    if (await Permission.photos.isGranted) {
      return true;
    } else if (await Permission.photos.isDenied) {
      await Permission.photos.request();
      if (await Permission.photos.isGranted) {
        return true;
      } else {
        throw ("您取消了相册授权");
      }
    } else if (await Permission.photos.isPermanentlyDenied) {
      throw ("您拒绝了相册授权，请在设置中打开");
    } else {
      throw ("相册授权未知错误");
    }
  }

  /// 定位适用用
  static Future<bool> locationPermissionRequest() async {
    return await location();
  }

  /// 拍视频适用
  static Future<bool> videoPermissionRequest() async {
    return await camera() &&
        await microphone() &&
        await storage() &&
        await photos();
  }

  /// 拍照片适用
  static Future<bool> cameraPermissionRequest() async {
    return await camera() && await storage() && await photos();
  }

  /// 扫描适用
  static Future<bool> qrCodePermissionRequest() async {
    return await camera();
  }

  /// 上传下载适用
  static Future<bool> filePermissionRequest() async {
    return await storage() && await photos();
  }
}
