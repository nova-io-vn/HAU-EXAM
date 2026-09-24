const MOBILE_REPOSITORY_URL = 'https://github.com/nova-io-vn/hau-exam-app'
const ANDROID_APK_URL = `${MOBILE_REPOSITORY_URL}/releases/latest/download/HAU-EXAM.apk`

export const DOWNLOAD_CONFIG = {
  android: {
    enabled: true,
    version: '1.0.0',
    build: '1',
    size: null,
    updatedAt: null,
    minOs: null,
    fileType: 'APK',
    releaseStatus: 'Sẵn sàng',
    downloadUrl: ANDROID_APK_URL,
    whatsNew: ['Ngân hàng câu hỏi', 'Quản lý bài thi', 'Thông báo', 'AI hỗ trợ'],
  },
  ios: {
    enabled: true,
    version: '1.0.0',
    expoSdk: '57',
    method: 'Expo Go',
    qrImage: '/assets/qr.png',
    expoUrl: null,
    expoGoStoreUrl: 'https://apps.apple.com/us/app/expo-go/id982107779',
    releaseStatus: 'Expo Go',
    whatsNew: ['Ngân hàng câu hỏi', 'Quản lý bài thi', 'Thông báo', 'AI hỗ trợ'],
  },
  repositoryUrl: MOBILE_REPOSITORY_URL,
}
