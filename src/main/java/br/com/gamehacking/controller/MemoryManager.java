package br.com.gamehacking.controller;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Objects;

@ApplicationScoped
public class MemoryManager {
    private final Kernel32 kernel32 = Kernel32.INSTANCE;

    public String changeMemory(String processName, long address, long value, int size) {
        Pointer buffer = new Memory(size);
        buffer.write(0, longToByteArray(value), 0, size);
        WinNT.HANDLE hProcess = openProcess(processName);
        IntByReference bytesWritten = new IntByReference();
        boolean success = kernel32.WriteProcessMemory(hProcess, new Pointer(address), buffer, size, bytesWritten);
        if (!success) {
            throw new RuntimeException("Falha ao escrever na memória do processo externo.");
        }
        kernel32.CloseHandle(hProcess);
        return "Bytes escritos: " + value;
    }

    public String readMemory(String processName, long address, int size) {
        Pointer buffer = new Memory(size);
        WinNT.HANDLE hProcess = openProcess(processName);
        boolean success = kernel32.ReadProcessMemory(hProcess, new Pointer(address), buffer, size, null);
        if (!success) {
            throw new RuntimeException("Falha ao ler a memória do processo externo.");
        }
        kernel32.CloseHandle(hProcess);
        return "Valor lido da memória do processo externo: 0x" + Integer.toHexString(getValueAddress(buffer, size));
    }

    private static byte[] longToByteArray(long value) {
        byte[] byteArray = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteArray[i] = (byte) (value >> (i * 8));
        }
        return byteArray;
    }

    private int getValueAddress(Pointer buffer, int size) {
        switch(size) {
            case 1: return buffer.getByte(0);
            case 2: return buffer.getShort(0);
            case 4: return buffer.getInt(0);
        }
        return 0;
    }

    private int getProcessId(String processName) {
        WinNT.HANDLE snapshot = kernel32.CreateToolhelp32Snapshot(new WinNT.DWORD(0x00000002), new WinNT.DWORD(0));
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();
        try {
            while (kernel32.Process32Next(snapshot, processEntry)) {
                String currentProcessName = Native.toString(processEntry.szExeFile).toLowerCase();
                if (currentProcessName.equals(processName.toLowerCase())) {
                    return processEntry.th32ProcessID.intValue();
                }
            }
        } finally {
            kernel32.CloseHandle(snapshot);
        }
        return -1;
    }

    private WinNT.HANDLE openProcess(String processName) {
        int processId = getProcessId(processName);
        if(processId == -1) {
            throw new RuntimeException("Processo não encontrado.");
        }
        WinNT.HANDLE hProcess = kernel32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false, processId);
        if(Objects.isNull(hProcess)) {
            throw new RuntimeException("Não foi possível obter uma handle para o processo.");
        }
        return hProcess;
    }
}