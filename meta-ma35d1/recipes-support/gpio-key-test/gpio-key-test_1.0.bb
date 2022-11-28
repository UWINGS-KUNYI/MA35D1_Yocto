
SUMMARY = "GPIO-KEY Simple Test Application"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://gpio-key-test.c;sha256=88abdf47fa973a0d0bc971e7dcda0b5d23dc8a65bac96783ded8242f280638ff"

PV = "1.0"
PR = "r0"
SRC_URI = "file://gpio-key-test.c \
	   file://Makefile"

S = "${WORKDIR}"
TARGET_CC_ARCH += " ${LDFLAGS}"

do_compile() {
	make
}

do_install() {
	install -d ${D}${sbindir}
	install -m 0755 ${S}/gpio-key-test ${D}${sbindir}
}

FILES:${PN} = "${sbindir}/gpio-key-test"
