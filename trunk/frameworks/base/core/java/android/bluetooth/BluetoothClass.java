/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 代表一个描述了设备通用特性和功能的蓝牙类.比如，一个蓝牙类会指定皆如电话、
 * 计算机或耳机的通用设备类型，可以提供皆如音频或者电话的服务.
 * <p>每个蓝牙类都是有0个或更多的服务类，以及一个设备类组成.
 * 设备类将被分解成主要和较小的设备类部分.</p>
 * <p>{@link BluetoothClass} 用作一个能粗略描述一个设备
 * （比如关闭用户界面上一个图标的设备）的线索，但当蓝牙服务事实上是被一个设备所支撑的时候，
 * BluetoothClass的 介绍则不那么可信任.精确的服务搜寻通过SDP请求来完成.当运用
 * {@link BluetoothDevice#createRfcommSocketToServiceRecord(UUID)} 和
 * {@link BluetoothAdapter#listenUsingRfcommWithServiceRecord(String,UUID)}
 * 来创建RFCOMM端口的时候，SDP请求就会自动执行.</p>
 * <p>使用 {@link BluetoothDevice#getBluetoothClass} 方法来获取为远程设备所提供的类.</p>
 *
 * <!--
 * The Bluetooth class is a 32 bit field. The format of these bits is defined at
 * http://www.bluetooth.org/Technical/AssignedNumbers/baseband.htm
 * (login required). This class contains that 32 bit field, and provides
 * constants and methods to determine which Service Class(es) and Device Class
 * are encoded in that field.
 * -->
 * 
 * @author translate by Android Club SYSU
 * @author translate by cnmahj
 * @author convert by cnmahj
 */
public final class BluetoothClass implements Parcelable {
    /**
     * Legacy error value. Applications should use null instead.
     * @hide
     */
    public static final int ERROR = 0xFF000000;

    private final int mClass;

    /** @hide */
    public BluetoothClass(int classInt) {
        mClass = classInt;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BluetoothClass) {
            return mClass == ((BluetoothClass)o).mClass;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mClass;
    }

    @Override
    public String toString() {
        return Integer.toHexString(mClass);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<BluetoothClass> CREATOR =
            new Parcelable.Creator<BluetoothClass>() {
        public BluetoothClass createFromParcel(Parcel in) {
            return new BluetoothClass(in.readInt());
        }
        public BluetoothClass[] newArray(int size) {
            return new BluetoothClass[size];
        }
    };

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mClass);
    }

    /**
     * 定义所有的服务类常量.
     * <p>每个 {@link BluetoothClass} 由0或多个服务类编码.
     */
    public static final class Service {
        private static final int BITMASK                 = 0xFFE000;

        public static final int LIMITED_DISCOVERABILITY = 0x002000;
        public static final int POSITIONING             = 0x010000;
        public static final int NETWORKING              = 0x020000;
        public static final int RENDER                  = 0x040000;
        public static final int CAPTURE                 = 0x080000;
        public static final int OBJECT_TRANSFER         = 0x100000;
        public static final int AUDIO                   = 0x200000;
        public static final int TELEPHONY               = 0x400000;
        public static final int INFORMATION             = 0x800000;
    }

    /**
     * 如果 {@link BluetoothClass} 支持指定的服务，则返回真. 
     * <p>在 {@link BluetoothClass.Service} 
     * 中的代表服务类的公共常量可用于该方法.
     * 例如 {@link BluetoothClass.Service#AUDIO}类.
     *
     * @param service 可用的服务类常量.
     * @return 如果支持指定的服务，返回真.
     */
    public boolean hasService(int service) {
        return ((mClass & Service.BITMASK & service) != 0);
    }

    /**
     * 定义所有的设备类常量.
     * <p>每个 {@link BluetoothClass} 对主次分类完全匹配的设备类进行编码.
     * <p>{@link BluetoothClass.Device}中的常量代表主次设备组件的组合
     * （完整的设备类）.{@link BluetoothClass.Device.Major} 
     * 中的常量只能代表主要设备类.
     * <p>参见服务类的常量 {@link BluetoothClass.Service}.
     */
    public static class Device {
        private static final int BITMASK               = 0x1FFC;

        /**
         * Defines all major device class constants.
         * <p>See {@link BluetoothClass.Device} for minor classes.
         */
        public static class Major {
            private static final int BITMASK           = 0x1F00;

            public static final int MISC              = 0x0000;
            public static final int COMPUTER          = 0x0100;
            public static final int PHONE             = 0x0200;
            public static final int NETWORKING        = 0x0300;
            public static final int AUDIO_VIDEO       = 0x0400;
            public static final int PERIPHERAL        = 0x0500;
            public static final int IMAGING           = 0x0600;
            public static final int WEARABLE          = 0x0700;
            public static final int TOY               = 0x0800;
            public static final int HEALTH            = 0x0900;
            public static final int UNCATEGORIZED     = 0x1F00;
        }

        // Devices in the COMPUTER major class
        public static final int COMPUTER_UNCATEGORIZED              = 0x0100;
        public static final int COMPUTER_DESKTOP                    = 0x0104;
        public static final int COMPUTER_SERVER                     = 0x0108;
        public static final int COMPUTER_LAPTOP                     = 0x010C;
        public static final int COMPUTER_HANDHELD_PC_PDA            = 0x0110;
        public static final int COMPUTER_PALM_SIZE_PC_PDA           = 0x0114;
        public static final int COMPUTER_WEARABLE                   = 0x0118;

        // Devices in the PHONE major class
        public static final int PHONE_UNCATEGORIZED                 = 0x0200;
        public static final int PHONE_CELLULAR                      = 0x0204;
        public static final int PHONE_CORDLESS                      = 0x0208;
        public static final int PHONE_SMART                         = 0x020C;
        public static final int PHONE_MODEM_OR_GATEWAY              = 0x0210;
        public static final int PHONE_ISDN                          = 0x0214;

        // Minor classes for the AUDIO_VIDEO major class
        public static final int AUDIO_VIDEO_UNCATEGORIZED           = 0x0400;
        public static final int AUDIO_VIDEO_WEARABLE_HEADSET        = 0x0404;
        public static final int AUDIO_VIDEO_HANDSFREE               = 0x0408;
        //public static final int AUDIO_VIDEO_RESERVED              = 0x040C;
        public static final int AUDIO_VIDEO_MICROPHONE              = 0x0410;
        public static final int AUDIO_VIDEO_LOUDSPEAKER             = 0x0414;
        public static final int AUDIO_VIDEO_HEADPHONES              = 0x0418;
        public static final int AUDIO_VIDEO_PORTABLE_AUDIO          = 0x041C;
        public static final int AUDIO_VIDEO_CAR_AUDIO               = 0x0420;
        public static final int AUDIO_VIDEO_SET_TOP_BOX             = 0x0424;
        public static final int AUDIO_VIDEO_HIFI_AUDIO              = 0x0428;
        public static final int AUDIO_VIDEO_VCR                     = 0x042C;
        public static final int AUDIO_VIDEO_VIDEO_CAMERA            = 0x0430;
        public static final int AUDIO_VIDEO_CAMCORDER               = 0x0434;
        public static final int AUDIO_VIDEO_VIDEO_MONITOR           = 0x0438;
        public static final int AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER = 0x043C;
        public static final int AUDIO_VIDEO_VIDEO_CONFERENCING      = 0x0440;
        //public static final int AUDIO_VIDEO_RESERVED              = 0x0444;
        public static final int AUDIO_VIDEO_VIDEO_GAMING_TOY        = 0x0448;

        // Devices in the WEARABLE major class
        public static final int WEARABLE_UNCATEGORIZED              = 0x0700;
        public static final int WEARABLE_WRIST_WATCH                = 0x0704;
        public static final int WEARABLE_PAGER                      = 0x0708;
        public static final int WEARABLE_JACKET                     = 0x070C;
        public static final int WEARABLE_HELMET                     = 0x0710;
        public static final int WEARABLE_GLASSES                    = 0x0714;

        // Devices in the TOY major class
        public static final int TOY_UNCATEGORIZED                   = 0x0800;
        public static final int TOY_ROBOT                           = 0x0804;
        public static final int TOY_VEHICLE                         = 0x0808;
        public static final int TOY_DOLL_ACTION_FIGURE              = 0x080C;
        public static final int TOY_CONTROLLER                      = 0x0810;
        public static final int TOY_GAME                            = 0x0814;

        // Devices in the HEALTH major class
        public static final int HEALTH_UNCATEGORIZED                = 0x0900;
        public static final int HEALTH_BLOOD_PRESSURE               = 0x0904;
        public static final int HEALTH_THERMOMETER                  = 0x0908;
        public static final int HEALTH_WEIGHING                     = 0x090C;
        public static final int HEALTH_GLUCOSE                      = 0x0910;
        public static final int HEALTH_PULSE_OXIMETER               = 0x0914;
        public static final int HEALTH_PULSE_RATE                   = 0x0918;
        public static final int HEALTH_DATA_DISPLAY                 = 0x091C;
    }

    /**
     * 返回该 {@link BluetoothClass} 中的主设备类型.
     * <p>通过将函数的返回值与 {@link BluetoothClass.Device.Major} 
     * 中的公共常量做比较，可以确定该 BluetoothClass 用于为那种主要设备进行编码.
     *
     * @return major 主设备分类.
     */
    public int getMajorDeviceClass() {
        return (mClass & Device.Major.BITMASK);
    }

    /**
     * 返回该 {@link BluetoothClass} 的设备类型(包含主次设备).
     * <p>通过将函数的返回值与 {@link BluetoothClass.Device} 
     * 中的公共常量做比较，可以确定该 BluetoothClass 用于为那种设备进行编码.
     *
     * @return 设备分类.
     */
    public int getDeviceClass() {
        return (mClass & Device.BITMASK);
    }

    /** @hide */
    public static final int PROFILE_HEADSET = 0;
    /** @hide */
    public static final int PROFILE_A2DP = 1;
    /** @hide */
    public static final int PROFILE_OPP = 2;

    /**
     * Check class bits for possible bluetooth profile support.
     * This is a simple heuristic that tries to guess if a device with the
     * given class bits might support specified profile. It is not accurate for all
     * devices. It tries to err on the side of false positives.
     * @param profile The profile to be checked
     * @return True if this device might support specified profile.
     * @hide
     */
    public boolean doesClassMatch(int profile) {
        if (profile == PROFILE_A2DP) {
            if (hasService(Service.RENDER)) {
                return true;
            }
            // By the A2DP spec, sinks must indicate the RENDER service.
            // However we found some that do not (Chordette). So lets also
            // match on some other class bits.
            switch (getDeviceClass()) {
                case Device.AUDIO_VIDEO_HIFI_AUDIO:
                case Device.AUDIO_VIDEO_HEADPHONES:
                case Device.AUDIO_VIDEO_LOUDSPEAKER:
                case Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (hasService(Service.RENDER)) {
                return true;
            }
            // Just in case they forgot the render service class
            switch (getDeviceClass()) {
                case Device.AUDIO_VIDEO_HANDSFREE:
                case Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                case Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_OPP) {
            if (hasService(Service.OBJECT_TRANSFER)) {
                return true;
            }

            switch (getDeviceClass()) {
                case Device.COMPUTER_UNCATEGORIZED:
                case Device.COMPUTER_DESKTOP:
                case Device.COMPUTER_SERVER:
                case Device.COMPUTER_LAPTOP:
                case Device.COMPUTER_HANDHELD_PC_PDA:
                case Device.COMPUTER_PALM_SIZE_PC_PDA:
                case Device.COMPUTER_WEARABLE:
                case Device.PHONE_UNCATEGORIZED:
                case Device.PHONE_CELLULAR:
                case Device.PHONE_CORDLESS:
                case Device.PHONE_SMART:
                case Device.PHONE_MODEM_OR_GATEWAY:
                case Device.PHONE_ISDN:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
}
