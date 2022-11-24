require recipes-security/optee/optee-os.inc

SUMMARY:ma35d1 = "OP-TEE Trusted OS for Nuvoton MA35D1"
DEPENDS:append:ma35d1 = " dtc-native python3-pycryptodomex-native python3-pycryptodome-native"
SRC_URI:append:ma35d1 = " file://0001-add-nuvoton-ma35d1-support.patch"
SRCREV = "1ee647035939e073a2e8dddb727c0f019cc035f1"

do_compile:ma35d1() {
	if echo ${TFA_DTB} | grep -q "128"; then
            oe_runmake CFG_TZDRAM_START=0x87800000 CFG_SHMEM_START=0x87F00000 -C ${S} O=${B}
	elif echo ${TFA_DTB} | grep -q "512"; then
            oe_runmake CFG_TZDRAM_START=0x9F800000 CFG_SHMEM_START=0x9FF00000 -C ${S} O=${B}
	elif echo ${TFA_DTB} | grep -q "1g"; then
            oe_runmake CFG_TZDRAM_START=0xAF800000 CFG_SHMEM_START=0xAFF00000 -C ${S} O=${B}
	else
            oe_runmake -C ${S} O=${B}
	fi
}

OPTEE_BOOTCHAIN = "optee"
OPTEE_HEADER    = "tee-header_v2"
OPTEE_PAGEABLE  = "tee-pageable_v2"
OPTEE_PAGER     = "tee-pager_v2"
OPTEE_SUFFIX    = "bin"
# Output the ELF generated
OPTEE_ELF = "tee"
OPTEE_ELF_SUFFIX = "elf"
do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/core/${OPTEE_HEADER}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_HEADER}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_PAGER}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_PAGER}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_PAGEABLE}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_PAGEABLE}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_ELF}.${OPTEE_ELF_SUFFIX} ${DEPLOYDIR}/${OPTEE_ELF}-${OPTEE_BOOTCHAIN}.${OPTEE_ELF_SUFFIX}
}
addtask deploy before do_build after do_compile
