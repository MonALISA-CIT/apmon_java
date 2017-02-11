#include <jni.h>
#include "NativeLinux.h"
#include <sys/types.h>
#include <unistd.h> 

JNIEXPORT jlong JNICALL Java_apmon_util_NativeLinux_mygetpid (JNIEnv *env, jobject obj) {
  return getpid();
}
