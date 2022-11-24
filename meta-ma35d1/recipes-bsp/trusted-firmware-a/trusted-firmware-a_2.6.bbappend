FILESEXTRAPATHS:prepend:ma35d1 := "${THISDIR}/files:"

COMPATIBLE_MACHINE:ma35d1 = "(ma35d1)"
TFA_BUILD_TARGET:ma35d1 = "all fiptool"
TFA_INSTALL_TARGET:ma35d1 = "bl31"
TFA_SPD:ma35d1 = "opteed"

SRC_URI:append:ma35d1 = " file://0001-add-nuvoton-ma35d1-support.patch"

PLATFORM="ma35d1"

do_compile:ma35d1() {
    # This is still needed to have the native tools executing properly by
    # setting the RPATH
    sed -i '/^LDLIBS/ s,$, \$\{BUILD_LDFLAGS},' ${S}/tools/fiptool/Makefile
    sed -i '/^INCLUDE_PATHS/ s,$, \$\{BUILD_CFLAGS},' ${S}/tools/fiptool/Makefile
    sed -i '/^LIB/ s,$, \$\{BUILD_LDFLAGS},' ${S}/tools/cert_create/Makefile

    TFA_OPT="NEED_BL31=yes NEED_BL33=yes"
    if [ "${TFA_LOAD_M4}" = "yes" ]; then
        TFA_OPT="${TFA_OPT} NEED_SCP_BL2=yes"
    fi

    MEM_OPT=""
    if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
        TFA_OPT="${TFA_OPT} NEED_BL32=yes"

        if echo ${TFA_DTB} | grep -q "256"; then
            MEM_OPT="" # code default for 256MiB
        elif echo ${TFA_DTB} | grep -q "128"; then
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_SIZE=0x07800000"
            MEM_OPT="${MEM_OPT} MA35D1_DDR_MAX_SIZE=0x8000000"
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_S_BASE=0x87800000"
            MEM_OPT="${MEM_OPT} MA35D1_BL32_BASE=0x87800000"
        elif echo ${TFA_DTB} | grep -q "512"; then
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_SIZE=0x1F800000"
            MEM_OPT="${MEM_OPT} MA35D1_DDR_MAX_SIZE=0x20000000"
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_S_BASE=0x9F800000"
            MEM_OPT="${MEM_OPT} MA35D1_BL32_BASE=0x9F800000"
        elif echo ${TFA_DTB} | grep -q "1g"; then
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_SIZE=0x3F800000"
            MEM_OPT="${MEM_OPT} MA35D1_DDR_MAX_SIZE=0x40000000"
            MEM_OPT="${MEM_OPT} MA35D1_DRAM_S_BASE=0xBF800000"
            MEM_OPT="${MEM_OPT} MA35D1_BL32_BASE=0xBF800000"
        fi
    fi

    oe_runmake ${TFA_OPT} ${MEM_OPT} -C ${S} realclean
    # Currently there are races if you build all the targets at once in parallel
    for T in ${TFA_BUILD_TARGET}; do
        oe_runmake ${TFA_OPT} ${MEM_OPT} -C ${S} $T
    done
}

do_deploy:append:ma35d1() {
    install -Dm 0644 ${B}/${PLATFORM}/release/bl2.bin ${DEPLOYDIR}/${BOOT_TOOLS}/bl2-${PLATFORM}.bin
    install -Dm 0644 ${B}/${PLATFORM}/release/fdts/${TFA_DTB}.dtb ${DEPLOYDIR}/${BOOT_TOOLS}/bl2-${PLATFORM}.dtb
    install -Dm 0644 ${B}/${PLATFORM}/release/bl31.bin ${DEPLOYDIR}/${BOOT_TOOLS}/bl31-${PLATFORM}.bin
    install -Dm 755 ${S}/tools/fiptool/fiptool  ${DEPLOYDIR}/${BOOT_TOOLS}/fiptool
}
