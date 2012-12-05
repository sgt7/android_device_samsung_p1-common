ifneq ($(filter p1 p1c p1l p1n,$(TARGET_DEVICE)),)
    include $(all-subdir-makefiles)
endif
