package com.f23port.bixbyfix;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Hook implements IXposedHookLoadPackage {
    private static final String PKG = "com.samsung.android.bixby.wakeup";
    private static final String TAG = "F23BixbyWakeFixV5";
    private static final Set<String> hooked = new HashSet<>();

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!PKG.equals(lpparam.packageName)) return;
        XposedBridge.log(TAG + ": loaded in Bixby Wakeup");

        hookBooleanMethods("com.samsung.android.voicewakeup.alwaysmicon.ApWakeupService", lpparam.classLoader);
        hookBooleanMethods("com.samsung.android.voicewakeup.alwaysmicon.AecWakeupService", lpparam.classLoader);
    }

    private static void hookBooleanMethods(String className, ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClass(className, cl);
            for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
                for (Method m : k.getDeclaredMethods()) {
                    if (m.getParameterTypes().length != 0 || m.getReturnType() != boolean.class) continue;
                    final Method method = m;
                    String key = className + "#" + method.toGenericString();
                    synchronized (hooked) { if (!hooked.add(key)) continue; }
                    try {
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override protected void afterHookedMethod(MethodHookParam p) throws Throwable {
                                Object r = p.getResult();
                                if (!(r instanceof Boolean) || ((Boolean) r)) return;
                                if (!duringAecRestart()) return;
                                XposedBridge.log(TAG + ": forcing false->true: " + method);
                                p.setResult(Boolean.TRUE);
                            }
                        });
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + ": hook failed " + method + " : " + t);
                    }
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": class failed " + className + " : " + t);
        }
    }

    private static boolean duringAecRestart() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        boolean inAec = false;
        boolean restart = false;
        for (StackTraceElement e : st) {
            String c = e.getClassName();
            String m = e.getMethodName();
            if (c.contains("AecWakeupService") || c.contains("ApWakeupService")) inAec = true;
            if (m.contains("onRestart") || m.contains("restart")) restart = true;
        }
        return inAec && restart;
    }
}
