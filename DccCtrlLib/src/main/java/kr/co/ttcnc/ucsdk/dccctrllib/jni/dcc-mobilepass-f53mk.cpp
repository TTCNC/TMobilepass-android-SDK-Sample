
//
// Created by giftn on 2019-08-13.
// DCSDK Library 를 Build 한 다음
// build\intermediates\ndkBuild\release\obj\local 폴더의
// arm64-v8a 폴더와 armeabi-v7a 폴더를 'jniLibs" 아래로 복사한 후
// 2 개의 폴더 내에 있는 obj 폴더를 삭제한다.
//
#include <jni.h>
#include "kr_co_ttcnc_ucsdk_dccctrllib_JNIClass.h"
extern   "C"
JNIEXPORT  jbyteArray  JNICALL
Java_kr_co_ttcnc_ucsdk_dccctrllib_JNIClass_getf53TokenIVValue
        ( JNIEnv   * env ,   jobject  type )   {

    unsigned char   Key [16] = {0xf5,0x06,0x31,0x23,0x5e,0xa9,0x24,0x54,0x13,0x8d,0x5e,0xd9,0x87,0x70,0x90,0x91};
    jbyteArray  ret   =   env -> NewByteArray ( 16 ) ;

    env -> SetByteArrayRegion (ret , 0 , 16 , reinterpret_cast<const jbyte *>(Key)) ;

    return   ret ;
}

 extern   "C"
 JNIEXPORT  jbyteArray  JNICALL
 Java_kr_co_ttcnc_ucsdk_dccctrllib_JNIClass_getf53MKValue
         ( JNIEnv   * env ,   jobject  type )   {

     unsigned char   Key [16] = {0x34, 0x6A, 0xA3, 0xC5, 0x44, 0x45, 0xAC, 0xF4,0x81, 0xDE, 0x6E, 0x15, 0x45, 0x1D, 0xB1, 0x4E}; ;
     jbyteArray  ret   =   env -> NewByteArray ( 16 ) ;

     env -> SetByteArrayRegion (ret , 0 , 16 , reinterpret_cast<const jbyte *>(Key)) ;

     return   ret ;

 }

extern   "C"
JNIEXPORT  jbyteArray  JNICALL
Java_kr_co_ttcnc_ucsdk_dccctrllib_JNIClass_getAes192MKValue
        ( JNIEnv   * env ,   jobject  type )   {

    unsigned char   Key [24] = {
            0xfe, 0xff, 0xe9, 0x92, 0x86, 0x65, 0x73, 0x1c, 0x6d, 0x6a, 0x8f, 0x94, 0x67, 0x30, 0x83, 0x08,
            0x34, 0x6A, 0xA3, 0xC5, 0x44, 0x45, 0xAC, 0xF4 };
    jbyteArray  ret   =   env -> NewByteArray ( 24 ) ;

    env -> SetByteArrayRegion (ret , 0 , 24 , reinterpret_cast<const jbyte *>(Key)) ;

    return   ret ;

}

extern   "C"
JNIEXPORT  jbyteArray  JNICALL
Java_kr_co_ttcnc_ucsdk_dccctrllib_JNIClass_getAes256MKValue
( JNIEnv   * env ,   jobject  type )   {

/*
unsigned char   Key [32] = {
        0xfe, 0xff, 0xe9, 0x92, 0x86, 0x65, 0x73, 0x1c, 0x6d, 0x6a, 0x8f, 0x94, 0x67, 0x30, 0x83, 0x08,
        0x34, 0x6A, 0xA3, 0xC5, 0x44, 0x45, 0xAC, 0xF4, 0x81, 0xDE, 0x6E, 0x15, 0x45, 0x1D, 0xB1, 0x4E};
*/
unsigned char   Key [32] = {
        0x16, 0x16, 0xa7, 0xa5, 0x06, 0x03, 0xa0, 0x5c, 0x87, 0xec, 0x82, 0xf5, 0x35, 0x23, 0xfb, 0xa7, 0xf5, 0x06, 0x31, 0x23, 0x5e, 0xa9, 0x24, 0x54, 0x13, 0x8d, 0x5e, 0xd9, 0x87, 0x70, 0x90, 0x91};
    jbyteArray  ret   =   env -> NewByteArray ( 32 ) ;

env -> SetByteArrayRegion (ret , 0 , 32 , reinterpret_cast<const jbyte *>(Key)) ;

return   ret ;

}