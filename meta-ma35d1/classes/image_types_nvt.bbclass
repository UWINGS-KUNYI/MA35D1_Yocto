inherit image_types

DEPENDS = "\
    gcc-arm-none-eabi-native \
    parted-native \
    python3-nuwriter-native \
"

IMAGE_TYPEDEP:nand = "ubi"
do_image:nand[depends] = "\
    trusted-firmware-a:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'optee-os:do_deploy', '',d)} \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    python3-nuwriter-native:do_install \
    jq-native:do_populate_sysroot \
    mtd-utils-native:do_populate_sysroot \
    m4proj:do_deploy \
"

IMAGE_TYPEDEP:spinand = "ubi"
do_image:spinand[depends] = "\
    trusted-firmware-a:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'optee-os:do_deploy', '',d)} \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    python3-nuwriter-native:do_install \
    jq-native:do_populate_sysroot \
    mtd-utils-native:do_populate_sysroot \
    m4proj:do_deploy \
"

IMAGE_TYPEDEP:sdcard = "ext4"
do_image:sdcard[depends] = "\
    parted-native:do_populate_sysroot \
    trusted-firmware-a:do_deploy \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'optee-os:do_deploy', '',d)} \
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    python3-nuwriter-native:do_install \
    jq-native:do_populate_sysroot \
    m4proj:do_deploy \
"

NUWRITER_DIR="${RECIPE_SYSROOT_NATIVE}${datadir}/nuwriter"
M4_OPJCOPY="${RECIPE_SYSROOT_NATIVE}${datadir}/gcc-arm-none-eabi/arm-none-eabi/bin/objcopy"

clearOlderArtifact() {
    case ${1} in
    spinand)
        type="spinand"
        ;;
    nand)
        type="nand"
        ;;
    sdcard)
        type="sdcard"
        ;;
    *)
        bberror "not support clear type artifcat: $1"
    esac

    imageName=""
    if [ -f ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}-enc-${type}.pack ]; then
        imageName="${IMAGE_BASENAME}-${MACHINE}-enc-${type}"
    elif [ -f ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}-${type}.pack ]; then
        imageName=${IMAGE_BASENAME}-${MACHINE}-${type}
    fi

    bbnote "clrOlderArtifact imageName:${imageName}"
    if [ "x${imageName}" != "" ]; then
        rm ${DEPLOY_DIR_IMAGE}/header-${imageName}.bin -f
        rm ${DEPLOY_DIR_IMAGE}/${imageName}.pack -f
        rm ${DEPLOY_DIR_IMAGE}/pack-${imageName}.bin -f
    fi
}

# Generate the FIP image  with the bl2.bin and required Device Tree
generateFIPImage() {
    case ${1} in
    spinand)
        type="spinand"
        ;;
    nand)
        type="nand"
        ;;
    sdcard)
        type="sdcard"
        ;;
    *)
        bberror "not support generate FIP image for type artifcat: $1"
    esac

    FIP_OPT=" --soc-fw ${DEPLOY_DIR_IMAGE}/bl31-${TFA_PLATFORM}.bin \
              --nt-fw ${DEPLOY_DIR_IMAGE}/u-boot.bin-${type}"

    OUTPUT="fip_without_optee-${IMAGE_BASENAME}-${MACHINE}.bin-${type}"

    if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
        FIP_OPT="${FIP_OPT} \
                    --tos-fw ${DEPLOY_DIR_IMAGE}/tee-header_v2-optee.bin \
                    --tos-fw-extra1 ${DEPLOY_DIR_IMAGE}/tee-pager_v2-optee.bin"
        OUTPUT="fip_with_optee-${IMAGE_BASENAME}-${MACHINE}.bin-${type}"
    fi

    if [ "${TFA_LOAD_M4}" != "no" ]; then
        FIP_OPT="${FIP_OPT} \
                    --scp-fw ${DEPLOY_DIR_IMAGE}/${TFA_M4_BIN}"
        if [ -f ${DEPLOY_DIR_IMAGE}/${TFA_M4_BIN} ]; then
            bberror "Could not found ${DEPLOY_DIR_IMAGE}/${TFA_M4_BIN}"
        fi
    fi

    bbnote "${DEPLOY_DIR_IMAGE}/fiptool create ${FIP_OPT} ${OUTPUT}"
    ${DEPLOY_DIR_IMAGE}/fiptool create ${FIP_OPT} ${OUTPUT}
    (cd ${DEPLOY_DIR_IMAGE}; ln -sf ${OUTPUT} fip.bin-${type})
}

IMAGE_CMD:spinand() {
    (clearOlderArtifact "spinand")
    (generateFIPImage "spinand")
    (cd ${DEPLOY_DIR_IMAGE}; ubinize ${UBINIZE_ARGS} -o u-boot-initial-env.ubi-spinand u-boot-initial-env-spinand-ubi.cfg)

    if [ -f ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi ]; then
        if [ "${SECURE_BOOT}" = "no" ]; then
            (cd ${DEPLOY_DIR_IMAGE}; \
             cp ${NUWRITER_DIR}/*-spinand.json  nuwriter; \
             ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi rootfs.ubi-spinand; \
             tmpConv=$(nuwriter/nuwriter -c nuwriter/header-spinand.json | awk '{ print $6 }'); \
             cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-spinand.bin; \
             tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-spinand.json | awk '{ print $6}'); \
             cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-spinand.bin; \
             ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-spinand.bin ${IMAGE_BASENAME}-${MACHINE}-spinand.pack; \
             rm -rf ${tmpConv}; rm -rf ${tmpPack};)
             if [ -f ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-spinand.dtb ]; then
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-spinand.dtb
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-spinand.bin
             fi
        else
            (cd ${DEPLOY_DIR_IMAGE}; \
             $(cat ${NUWRITER_DIR}/header-spinand.json | jq -r ".header.secureboot = \"yes\"" | \
             jq -r ".header.aeskey = \"${AES_KEY}\"" | jq -r ".header.ecdsakey = \"${ECDSA_KEY}\"" \
             > nuwriter/header-spinand.json); \
             ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi rootfs.ubi-spinand; \
             tmpConv=$(nuwriter/nuwriter -c nuwriter/header-spinand.json | awk '{ print $6 }'); \
             cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-enc-spinand.bin; \
             cp ${tmpConv}/enc_bl2-ma35d1.dtb enc_bl2-ma35d1-spinand.dtb; \
             cp ${tmpConv}/enc_bl2-ma35d1.bin enc_bl2-ma35d1-spinand.bin; \
             echo "{\""publicx"\": \""$(head -6 conv/header_key.txt | tail +6)"\", \
             \""publicy"\": \""$(head -7 conv/header_key.txt | tail +7)"\", \
             \""aeskey"\": \""$(head -2 conv/header_key.txt | tail +2)"\"}" | \
             jq  > nuwriter/otp_key-spinand.json; \
             $(cat ${NUWRITER_DIR}/pack-spinand.json | \
             jq 'setpath(["image",1,"file"];"enc_bl2-ma35d1-spinand.dtb")' | \
             jq 'setpath(["image",2,"file"];"enc_bl2-ma35d1-spinand.bin")' > nuwriter/pack-spinand.json); \
             tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-spinand.json | awk '{ print $6 }'); \
             cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-enc-spinand.bin; \
             ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-enc-spinand.bin ${IMAGE_BASENAME}-${MACHINE}-enc-spinand.pack; \
             rm -rf ${tmpConv}; rm -rf ${tmpPack};)
        fi
    fi
}

IMAGE_CMD:nand() {
    (clearOlderArtifact "nand")
    (generateFIPImage "nand")
    (cd ${DEPLOY_DIR_IMAGE}; ubinize ${UBINIZE_ARGS} -o u-boot-initial-env.ubi-nand u-boot-initial-env-nand-ubi.cfg)

    if [ -f ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi ]; then
        if [ "${SECURE_BOOT}" = "no" ]; then
           (cd ${DEPLOY_DIR_IMAGE}; \
            ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi rootfs.ubi-nand; \
            cp ${NUWRITER_DIR}/*-nand.json  nuwriter; \
            tmpConv=$(nuwriter/nuwriter -c nuwriter/header-nand.json | awk '{ print $6 }'); \
            cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-nand.bin; \
            tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-nand.json | awk '{ print $6 }'); \
            cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-nand.bin; \
            ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-nand.bin ${IMAGE_BASENAME}-${MACHINE}-nand.pack; \
            rm -rf ${tmpConv}; rm -rf ${tmpPack};)
            if [ -f ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-nand.dtb ]; then
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-nand.dtb
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-nand.bin
            fi
        else
           (cd ${DEPLOY_DIR_IMAGE}; \
            ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ubi rootfs.ubi-nand; \
            $(cat ${NUWRITER_DIR}/header-nand.json | jq -r ".header.secureboot = \"yes\"" | \
            jq -r ".header.aeskey = \"${AES_KEY}\"" | jq -r ".header.ecdsakey = \"${ECDSA_KEY}\"" \
            > nuwriter/header-nand.json); \
            tmpConv=$(nuwriter/nuwriter -c nuwriter/header-nand.json | awk '{ print $6 }'); \
            cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-enc-nand.bin; \
            cp ${tmpConv}/enc_bl2-ma35d1.dtb enc_bl2-ma35d1-nand.dtb; \
            cp ${tmpConv}/enc_bl2-ma35d1.bin enc_bl2-ma35d1-nand.bin; \
            echo "{\""publicx"\": \""$(head -6 conv/header_key.txt | tail +6)"\", \
            \""publicy"\": \""$(head -7 conv/header_key.txt | tail +7)"\", \
            \""aeskey"\": \""$(head -2 conv/header_key.txt | tail +2)"\"}" | \
            jq  > nuwriter/otp_key-nand.json; \
            $(cat ${NUWRITER_DIR}/pack-nand.json | \
            jq 'setpath(["image",1,"file"];"enc_bl2-ma35d1-nand.dtb")' | \
            jq 'setpath(["image",2,"file"];"enc_bl2-ma35d1-nand.bin")' > nuwriter/pack-nand.json); \
            tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-nand.json | awk '{ print $6 }'); \
            cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-enc-nand.bin; \
            ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-enc-nand.bin ${IMAGE_BASENAME}-${MACHINE}-enc-nand.pack; \
            rm -rf ${tmpConv}; rm -rf ${tmpPack};)
        fi
    fi
}

SDCARD ?= "${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.sdcard"

# Boot partition size [in KiB]
BOOT_SPACE ?= "32768"

# Set alignment in KiB
IMAGE_ROOTFS_ALIGNMENT ?= "4096"

IMAGE_CMD:sdcard() {
    BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE} - 1 )
    (clearOlderArtifact "sdcard")
    (generateFIPImage "sdcard" )

    if [ -f ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ext4 ]; then
        SDCARD_SIZE=$(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT} \+ $ROOTFS_SIZE \+ ${IMAGE_ROOTFS_ALIGNMENT})
        # Initialize a sparse file
        dd if=/dev/zero of=${SDCARD} bs=1 count=0 seek=$(expr 1024 \* ${SDCARD_SIZE})
        parted -s ${SDCARD} mklabel msdos
        parted -s ${SDCARD} unit KiB mkpart primary $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT}) $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT} \+ $ROOTFS_SIZE )
        parted ${SDCARD} print

        # MBR table for nuwriter
        dd if=/dev/zero of=${DEPLOY_DIR_IMAGE}/MBR.scdard.bin bs=1 count=0 seek=512
        dd if=${SDCARD} of=${DEPLOY_DIR_IMAGE}/MBR.scdard.bin conv=notrunc seek=0 count=1 bs=512
        if [ "${SECURE_BOOT}" = "no" ]; then
           (cd ${DEPLOY_DIR_IMAGE}; \
            cp ${NUWRITER_DIR}/*-sdcard.json  nuwriter; \
            ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ext4 rootfs.ext4-sdcard; \
            tmpConv=$(nuwriter/nuwriter -c nuwriter/header-sdcard.json | awk '{ print $6 }'); \
            cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-sdcard.bin; \
            $(cat nuwriter/pack-sdcard.json | jq 'setpath(["image",8,"offset"];"'$(expr ${BOOT_SPACE_ALIGNED} \* 1024 + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024)'")' > nuwriter/pack-sdcard-tmp.json); \
            cp nuwriter/pack-sdcard-tmp.json nuwriter/pack-sdcard.json; \
            rm nuwriter/pack-sdcard-tmp.json; \
            tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-sdcard.json | awk '{ print $6 }'); \
            cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-sdcard.bin; \
            ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-sdcard.bin ${IMAGE_BASENAME}-${MACHINE}-sdcard.pack; \
            rm -rf ${tmpConv}; rm -rf ${tmpPack};)
            if [ -f ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-sdcard.dtb ]; then
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-sdcard.dtb
                rm ${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-sdcard.bin
            fi
        else
           (cd ${DEPLOY_DIR_IMAGE}; \
            ln -sf ${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ext4 rootfs.ext4-sdcard; \
            $(cat ${NUWRITER_DIR}/header-sdcard.json | jq -r ".header.secureboot = \"yes\"" | \
            jq -r ".header.aeskey = \"${AES_KEY}\"" | jq -r ".header.ecdsakey = \"${ECDSA_KEY}\"" \
            > nuwriter/header-sdcard.json); \
            tmpConv=$(nuwriter/nuwriter -c nuwriter/header-sdcard.json | awk '{ print $6 }'); \
            cp ${tmpConv}/enc_bl2-ma35d1.dtb enc_bl2-ma35d1-sdcard.dtb; \
            cp ${tmpConv}/enc_bl2-ma35d1.bin enc_bl2-ma35d1-sdcard.bin; \
            echo "{\""publicx"\": \""$(head -6 conv/header_key.txt | tail +6)"\", \
            \""publicy"\": \""$(head -7 conv/header_key.txt | tail +7)"\", \
            \""aeskey"\": \""$(head -2 conv/header_key.txt | tail +2)"\"}" | \
            jq  > nuwriter/otp_key-sdcard.json; \
            cp ${tmpConv}/header.bin header-${IMAGE_BASENAME}-${MACHINE}-enc-sdcard.bin; \
            $(cat ${NUWRITER_DIR}/pack-sdcard.json | \
            jq 'setpath(["image",2,"file"];"enc_bl2-ma35d1-sdcard.dtb")' | \
            jq 'setpath(["image",3,"file"];"enc_bl2-ma35d1-sdcard.bin")' | \
            jq 'setpath(["image",8,"offset"];"'$(expr ${BOOT_SPACE_ALIGNED} \* 1024 + \
            ${IMAGE_ROOTFS_ALIGNMENT} \* 1024)'")' > nuwriter/pack-sdcard.json); \
            tmpPack=$(nuwriter/nuwriter -p nuwriter/pack-sdcard.json | awk '{ print $6 }'); \
            cp ${tmpPack}/pack.bin pack-${IMAGE_BASENAME}-${MACHINE}-enc-sdcard.bin; \
            ln -sf pack-${IMAGE_BASENAME}-${MACHINE}-enc-sdcard.bin ${IMAGE_BASENAME}-${MACHINE}-enc-sdcard.pack; \
            rm -rf ${tmpConv}; rm -rf ${tmpPack};)
        fi

        if [ "${SECURE_BOOT}" = "no" ]; then
            # 0x400, 1KB
            dd if=${DEPLOY_DIR_IMAGE}/header-${IMAGE_BASENAME}-${MACHINE}-sdcard.bin of=${SDCARD} conv=notrunc seek=2 bs=512
            # 0x20000, 128KiB, (dtbs for BL2)
            dd if=${DEPLOY_DIR_IMAGE}/bl2-ma35d1.dtb of=${SDCARD} conv=notrunc seek=256 bs=512
            # 0x30000, 192KB
            dd if=${DEPLOY_DIR_IMAGE}/bl2-ma35d1.bin of=${SDCARD} conv=notrunc seek=384 bs=512
        else
            # 0x400
            dd if=${DEPLOY_DIR_IMAGE}/header-${IMAGE_BASENAME}-${MACHINE}-enc-sdcard.bin of=${SDCARD} conv=notrunc seek=2 bs=512
            # 0x20000
            dd if=${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-sdcard.dtb of=${SDCARD} conv=notrunc seek=256 bs=512
            # 0x30000
            dd if=${DEPLOY_DIR_IMAGE}/enc_bl2-ma35d1-sdcard.bin of=${SDCARD} conv=notrunc seek=384 bs=512
        fi
        # 0x40000, 256KiB, U-Boot Environments Variables
        dd if=${DEPLOY_DIR_IMAGE}/u-boot-initial-env.bin-sdcard of=${SDCARD} conv=notrunc seek=512 bs=512
        # 0xC0000, 768KiB, FIT Images (U-Boot, OP-TEE)
        dd if=${DEPLOY_DIR_IMAGE}/fip.bin-sdcard of=${SDCARD} conv=notrunc seek=1536 bs=512
        # 0x2c0000, 2816KiB, 2MiB + 768KiB, (dtbs for Kernel)
        dd if=${DEPLOY_DIR_IMAGE}/$(basename ${KERNEL_DEVICETREE}) of=${SDCARD} conv=notrunc seek=5632 bs=512
        # 0x300000, 3MiB, Kernel Image
        dd if=${DEPLOY_DIR_IMAGE}/Image-${MACHINE}.bin of=${SDCARD} conv=notrunc seek=6144 bs=512
        # root fs
        dd if=${IMGDEPLOYDIR}/${IMAGE_NAME}.rootfs.ext4 of=${SDCARD} conv=notrunc,fsync seek=1 bs=$(expr ${BOOT_SPACE_ALIGNED} \* 1024 + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024)
    fi
}
