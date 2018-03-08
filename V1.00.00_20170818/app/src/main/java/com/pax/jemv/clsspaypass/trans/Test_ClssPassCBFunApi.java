package com.pax.jemv.clsspaypass.trans;

import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;
import com.pax.jemv.paypass.listener.IClssPassCBFun;

/**
 * Created by a on 2017-04-10.
 */

public class Test_ClssPassCBFunApi implements IClssPassCBFun {
    @Override
    public int sendDEKData(byte[] bytes, int i) {
        return 0;
    }// 回调函数在这实现 - by wfh

    @Override
    public int receiveDETData(ByteArray byteArray, byte[] bytes) {
        return 0;
    }

    @Override
    public int addAPDUToTransLog(ApduSendL2 apduSendL2, ApduRespL2 apduRespL2) {
        return 0;
    }

    @Override
    public int sendTransDataOutput(byte b) {
        return 0;
    }
    /**自动生成代码**/

}
