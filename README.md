# 📱 Colab Mobile

Google Colab trên điện thoại — giao diện desktop đầy đủ nhưng tối ưu hoàn toàn cho cảm ứng.

## Tính năng

| Tính năng | Mô tả |
|---|---|
| 🖥️ Desktop mode | Chạy Colab giao diện PC đầy đủ, không mất tính năng |
| 🎯 Floating Dock | Nút Run / Run All / Stop / Restart to, dễ bấm, kéo được |
| ⌨️ Shortcut Bar | Thanh phím tắt coder: Tab, Ctrl, `[]`, `{}`, `()`, `/`... |
| 👆 Gesture Nav | Vuốt mép trái → File Explorer, mép phải → Mục lục |
| 🔐 Google Login | Đăng nhập Google bình thường, không bị chặn |

## Build APK tự động

Mỗi lần push lên `main`, GitHub Actions tự động build và xuất file APK.

### Cách lấy APK

1. Vào tab **Actions** trên GitHub
2. Chọn workflow run mới nhất
3. Kéo xuống phần **Artifacts** → tải `colab-mobile-debug-xxx.apk`
4. Cài trên điện thoại (cần bật **Cài từ nguồn không xác định**)

### Release APK (tag)

```bash
git tag v1.0.0
git push origin v1.0.0
```

→ GitHub tự tạo Release với file APK đính kèm.

## Cài đặt Dev

```bash
npm install
npx expo start
```

## Stack

- **Expo 52** + React Native 0.76
- `react-native-webview` — WebView nhúng Colab
- JS/CSS injection — phóng to nút bấm, fix layout
- GitHub Actions + Gradle — build APK tự động
