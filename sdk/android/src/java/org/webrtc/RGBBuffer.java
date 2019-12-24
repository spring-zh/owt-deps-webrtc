/*
 * Copyright 2017 The WebRTC project authors. All Rights Reserved.
 *
 * Use of this source code is governed by a BSD-style license
 * that can be found in the LICENSE file in the root of the source
 * tree. An additional intellectual property rights grant can be found
 * in the file PATENTS.  All contributing project authors may
 * be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;

public class RGBBuffer implements VideoFrame.Buffer {
  private final int width;
  private final int height;
  private final int stride;
  private final int sliceHeight;
  private final ByteBuffer buffer;
  private final int colorType;
  private final RefCountDelegate refCountDelegate;

  //for color_type
  public final static int COLOR_ARGB = 0;
  public final static int COLOR_RGBA = 1;
  public final static int COLOR_BGRA = 2;
  public final static int COLOR_ABGR = 3;
  public final static int COLOR_RGB = 4;
  public final static int COLOR_BGR = 5;

  public RGBBuffer(int width, int height, int stride, int sliceHeight, ByteBuffer buffer, int colorType, 
      @Nullable Runnable releaseCallback) {
    this.width = width;
    this.height = height;
    this.stride = stride;
    this.sliceHeight = sliceHeight;
    this.buffer = buffer;
    this.colorType = colorType;
    this.refCountDelegate = new RefCountDelegate(releaseCallback);
  }

  @Override
  public ByteBuffer data() {
    return buffer;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public VideoFrame.I420Buffer toI420() {
    return (VideoFrame.I420Buffer) cropAndScale(0, 0, width, height, width, height);
  }

  @Override
  public void retain() {
    refCountDelegate.retain();
  }

  @Override
  public void release() {
    refCountDelegate.release();
  }

  @Override
  public VideoFrame.Buffer cropAndScale(
      int cropX, int cropY, int cropWidth, int cropHeight, int scaleWidth, int scaleHeight) {
    JavaI420Buffer newBuffer = JavaI420Buffer.allocate(scaleWidth, scaleHeight);
    nativeCropAndScale(cropX, cropY, cropWidth, cropHeight, scaleWidth, scaleHeight, buffer, width,
        height, stride, sliceHeight, newBuffer.getDataY(), newBuffer.getStrideY(),
        newBuffer.getDataU(), newBuffer.getStrideU(), newBuffer.getDataV(), newBuffer.getStrideV(), colorType);
    return newBuffer;
  }

  private static native void nativeCropAndScale(int cropX, int cropY, int cropWidth, int cropHeight,
      int scaleWidth, int scaleHeight, ByteBuffer src, int srcWidth, int srcHeight, int srcStride,
      int srcSliceHeight, ByteBuffer dstY, int dstStrideY, ByteBuffer dstU, int dstStrideU,
      ByteBuffer dstV, int dstStrideV, int colorType);
}
