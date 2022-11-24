FILESEXTRAPATHS:prepend:ma35d1 := "${THISDIR}/optee-os:"
require optee-os_3.18.0.bb

SUMMARY = "OP-TEE Trusted OS TA devkit"
DESCRIPTION = "OP-TEE TA devkit for build TAs"
HOMEPAGE = "https://www.op-tee.org/"

do_install:ma35d1() {
    #install TA devkit
    install -d ${D}${includedir}/optee/export-user_ta/
    for f in ${B}/export-ta_${OPTEE_ARCH}/* ; do
        cp -aR $f ${D}${includedir}/optee/export-user_ta/
    done
}

do_deploy:ma35d1() {
    echo "Do not inherit do_deploy from optee-os."
}

FILES:${PN}:ma35d1 = "${includedir}/optee/"
