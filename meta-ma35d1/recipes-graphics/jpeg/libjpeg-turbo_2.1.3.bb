SUMMARY = "Hardware accelerated JPEG compression/decompression library"
DESCRIPTION = "libjpeg-turbo is a derivative of libjpeg that uses SIMD instructions (MMX, SSE2, NEON) to accelerate baseline JPEG compression and decompression"
HOMEPAGE = "http://libjpeg-turbo.org/"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "\
    file://cdjpeg.h;endline=13;md5=8a61af33cc1c681cd5cc297150bbb5bd \
    file://jpeglib.h;endline=16;md5=52b5eaade8d5b6a452a7693dfe52c084 \
    file://djpeg.c;endline=11;md5=510b386442ab6a27ee241fc5669bc5ea \
"
inherit cmake pkgconfig

DEPENDS:append_x86-64_class-target = " nasm-native"
DEPENDS:append_x86_class-target = " nasm-native"

PROVIDES = "jpeg"

SRC_URI = "\
    ${SOURCEFORGE_MIRROR}/${BPN}/${BPN}-${PV}.tar.gz \
    file://0001-libjpeg-turbo-fix-package_qa-error.patch \
    ${@bb.utils.contains('JPEG_HW_DEC', 'yes', 'file://0002-libjpeg-turbo-vc8000.patch', '', d)} \
"

SRC_URI[sha256sum] = "467b310903832b033fe56cd37720d1b73a6a3bd0171dbf6ff0b620385f4f76d0"
UPSTREAM_CHECK_URI = "http://sourceforge.net/projects/libjpeg-turbo/files/"
UPSTREAM_CHECK_REGEX = "/libjpeg-turbo/files/(?P<pver>(\d+[\.\-_]*)+)/"

PE = "1"

# Drop-in replacement for jpeg
RPROVIDES:${PN} += "jpeg"
RREPLACES:${PN} += "jpeg"
RCONFLICTS:${PN} += "jpeg"

export NASMENV = "--reproducible --debug-prefix-map=${WORKDIR}=/usr/src/debug/${PN}/${EXTENDPE}${PV}-${PR}"

# Add nasm-native dependency consistently for all build arches is hard
EXTRA_OECMAKE:append_class-native = " -DWITH_SIMD=False"
EXTRA_OECMAKE:append_class-nativesdk = " -DWITH_SIMD=False"

EXTRA_OECMAKE:append_class-target = " -DWITH_VC8000=1"
EXTRA_OECMAKE:append_class-target_arm = " -DWITH_VC8000=1"

# Work around missing x32 ABI support
EXTRA_OECMAKE:append_class-target = " ${@bb.utils.contains("TUNE_FEATURES", "mx32", "-DWITH_SIMD=False", "", d)}"

# Work around missing non-floating point ABI support in MIPS
EXTRA_OECMAKE:append_class-target = " ${@bb.utils.contains("MIPSPKGSFX_FPU", "-nf", "-DWITH_SIMD=False", "", d)}"

EXTRA_OECMAKE:append_class-target_arm = " ${@bb.utils.contains("TUNE_FEATURES", "neon", "", "-DWITH_SIMD=False", d)}"
EXTRA_OECMAKE:append_class-target_armeb = " ${@bb.utils.contains("TUNE_FEATURES", "neon", "", "-DWITH_SIMD=False", d)}"

# Provide a workaround if Altivec unit is not present in PPC
EXTRA_OECMAKE:append_class-target_powerpc = " ${@bb.utils.contains("TUNE_FEATURES", "altivec", "", "-DWITH_SIMD=False", d)}"
EXTRA_OECMAKE:append_class-target_powerpc64 = " ${@bb.utils.contains("TUNE_FEATURES", "altivec", "", "-DWITH_SIMD=False", d)}"
EXTRA_OECMAKE:append_class-target_powerpc64le = " ${@bb.utils.contains("TUNE_FEATURES", "altivec", "", "-DWITH_SIMD=False", d)}"

DEBUG_OPTIMIZATION:append_armv4 = " ${@bb.utils.contains('TUNE_CCARGS', '-mthumb', '-fomit-frame-pointer', '', d)}"
DEBUG_OPTIMIZATION:append_armv5 = " ${@bb.utils.contains('TUNE_CCARGS', '-mthumb', '-fomit-frame-pointer', '', d)}"

PACKAGES =+ "jpeg-tools libturbojpeg"

DESCRIPTION:jpeg-tools = "The jpeg-tools package includes client programs to access libjpeg functionality.  These tools allow for the compression, decompression, transformation and display of JPEG files and benchmarking of the libjpeg library."
FILES:jpeg-tools = "${bindir}/*"

DESCRIPTION:libturbojpeg = "A SIMD-accelerated JPEG codec which provides only TurboJPEG APIs"
FILES:libturbojpeg = "${libdir}/libturbojpeg.so.*"

BBCLASSEXTEND = "native nativesdk"
