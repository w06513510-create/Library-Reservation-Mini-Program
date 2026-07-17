import { BASE_URL, CLIENT_ID } from '../config';
import { getToken } from './request';

export interface UploadResult {
  url: string;
  fileName?: string;
  type?: 'image' | 'video';
}

/**
 * 上传单个本地文件(临时路径)到基座内置接口 /resource/media/upload，返回 { url, type }。
 * 该接口 @SaCheckLogin，app_user 登录后即可用（无需后台 OSS 权限）。
 */
export function uploadFile(filePath: string): Promise<UploadResult> {
  const header: Record<string, string> = { clientid: CLIENT_ID };
  const token = getToken();
  if (token) header['Authorization'] = 'Bearer ' + token;
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/resource/media/upload',
      filePath,
      name: 'file',
      header,
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
          if (body && body.code === 200 && body.data && body.data.url) {
            resolve(body.data as UploadResult);
          } else {
            uni.showToast({ title: (body && body.msg) || '上传失败', icon: 'none' });
            reject(body);
          }
        } catch (e) {
          uni.showToast({ title: '上传响应解析失败', icon: 'none' });
          reject(e);
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' });
        reject(err);
      }
    });
  });
}

/** 选择图片并逐张上传，返回成功的 url 数组 */
export async function chooseAndUploadImages(count = 9): Promise<string[]> {
  const urls: string[] = [];
  const res: any = await new Promise((resolve) => {
    uni.chooseImage({ count, sizeType: ['compressed'], success: resolve, fail: () => resolve(null) });
  });
  if (!res || !res.tempFilePaths || !res.tempFilePaths.length) return urls;
  uni.showLoading({ title: '上传中', mask: true });
  try {
    for (const p of res.tempFilePaths as string[]) {
      try {
        const r = await uploadFile(p);
        if (r.url) urls.push(r.url);
      } catch (e) {
        // 单张失败跳过，继续其余
      }
    }
  } finally {
    uni.hideLoading();
  }
  return urls;
}

/** 选择单个视频并上传，返回 url(失败返回空串) */
export async function chooseAndUploadVideo(): Promise<string> {
  const res: any = await new Promise((resolve) => {
    uni.chooseVideo({ sourceType: ['album', 'camera'], maxDuration: 60, success: resolve, fail: () => resolve(null) });
  });
  if (!res || !res.tempFilePath) return '';
  uni.showLoading({ title: '上传中', mask: true });
  try {
    const r = await uploadFile(res.tempFilePath);
    return r.url || '';
  } catch (e) {
    return '';
  } finally {
    uni.hideLoading();
  }
}
