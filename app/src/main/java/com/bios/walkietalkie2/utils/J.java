package com.bios.walkietalkie2.utils;

import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.dispatcher.IRegister;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerActionListener;
import com.xuhao.didi.socket.common.interfaces.common_interfacies.server.IServerManager;

public class J {
    void test(){
        IRegister<IServerActionListener, IServerManager> register = OkSocket.server(123);
    }
}
