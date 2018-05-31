/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.sharecarble.bluetooth;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    /**
     * 通信通道
     */
//    public final static String CONTROL_CHARACTERISTIC = "0000FFF4-0000-1000-8000-00805F9B34FB";
    public final static String CONTROL_CHARACTERISTIC = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public final static String CONTROL_CHARACTERISTIC_2 = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    /**
     * 用于搜索过滤设备
     */
    public final static String SERVER_CHARACTERISTIC = "0000FFF0-0000-1000-8000-00805F9B34FB";

}

 