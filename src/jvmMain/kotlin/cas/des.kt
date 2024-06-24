/**
 * @author https://github.com/tsurumi-yizhou/JiShi-Android/blob/foss/app/src/main/kotlin/me/yihtseu/jishi/utils/crypto/des.kt
 */
package cas



private fun generateKeys(keyByte: ByteArray): Array<ByteArray> {
    val key = ByteArray(56)
    val keys = Array(16) {
        ByteArray(48)
    }

    val loop = intArrayOf(1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1)

    for (i in 0 until 7) {
        for ((j, k) in (0 until 8).zip(7 downTo 0)) {
            key[i * 8 + j] = keyByte[8 * k + i]
        }
    }

    for (i in 0 until 16) {
        for (j in 0 until loop[i]) {
            Pair(key[0], key[28]).apply {
                for (k in 0 until 27) {
                    key[k] = key[k + 1]
                    key[28 + k] = key[29 + k]
                }
                key[27] = first
                key[55] = second
            }
        }
        val tempKey = byteArrayOf(
            key[13], key[16], key[10], key[23], key[0], key[4], key[2], key[27],
            key[14], key[5], key[20], key[9], key[22], key[18], key[11], key[3],
            key[25], key[7], key[15], key[6], key[26], key[19], key[12], key[1],
            key[40], key[51], key[30], key[36], key[46], key[54], key[29], key[39],
            key[50], key[44], key[32], key[47], key[43], key[48], key[38], key[55],
            key[33], key[52], key[45], key[41], key[49], key[35], key[28], key[31]
        )
        tempKey.copyInto(keys[i])
    }
    return keys
}

private fun getBoxBinary(i: Int): String {
    return "%04d".format(i.toString(2).toInt())
}

private fun initPermute(originalData: ByteArray): ByteArray {
    val ipByte = ByteArray(64)
    for ((i, pair) in (0 until 4).zip((1..7 step 2).zip(0..6 step 2))) {
        for ((j, k) in (7 downTo 0).zip(0..7)) {
            ipByte[i * 8 + k] = originalData[j * 8 + pair.first]
            ipByte[i * 8 + k + 32] = originalData[j * 8 + pair.second]
        }
    }
    return ipByte
}

private fun expandPermute(rightData: ByteArray): ByteArray {
    val epByte = ByteArray(48)
    for (i in 0 until 8) {
        epByte[i * 6 + 0] = rightData[if (i == 0) {
            31
        } else {
            i * 4 - 1
        }]
        epByte[i * 6 + 1] = rightData[i * 4]
        epByte[i * 6 + 2] = rightData[i * 4 + 1]
        epByte[i * 6 + 3] = rightData[i * 4 + 2]
        epByte[i * 6 + 4] = rightData[i * 4 + 3]
        epByte[i * 6 + 5] = rightData[if (i == 7) {
            0
        } else {
            i * 4 + 4
        }]
    }
    return epByte
}

private fun xor(byteOne: ByteArray, byteTwo: ByteArray): ByteArray {
    return ByteArray(byteOne.size).apply {
        for (i in byteOne.indices) {
            set(i, (byteOne[i].toInt() xor byteTwo[i].toInt()).toByte())
        }
    }
}

private fun sBoxPermute(expandByte: ByteArray): ByteArray {
    val sBoxByte = ByteArray(32)
    val s1 = arrayOf(
        intArrayOf(14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7),
        intArrayOf(0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8),
        intArrayOf(4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0),
        intArrayOf(15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13)
    )

    val s2 = arrayOf(
        intArrayOf(15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10),
        intArrayOf(3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5),
        intArrayOf(0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15),
        intArrayOf(13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9)
    )

    val s3 = arrayOf(
        intArrayOf(10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8),
        intArrayOf(13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1),
        intArrayOf(13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7),
        intArrayOf(1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12)
    )

    val s4 = arrayOf(
        intArrayOf(7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15),
        intArrayOf(13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9),
        intArrayOf(10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4),
        intArrayOf(3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14)
    )

    val s5 = arrayOf(
        intArrayOf(2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9),
        intArrayOf(14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6),
        intArrayOf(4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14),
        intArrayOf(11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3)
    )

    val s6 = arrayOf(
        intArrayOf(12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11),
        intArrayOf(10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8),
        intArrayOf(9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6),
        intArrayOf(4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13)
    )

    val s7 = arrayOf(
        intArrayOf(4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1),
        intArrayOf(13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6),
        intArrayOf(1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2),
        intArrayOf(6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12)
    )

    val s8 = arrayOf(
        intArrayOf(13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7),
        intArrayOf(1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2),
        intArrayOf(7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8),
        intArrayOf(2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11)
    )

    for (m in 0 until 8) {
        val i = expandByte[m * 6] * 2 +
                expandByte[m * 6 + 5]
        val j = expandByte[m * 6 + 1] * 2 * 2 * 2 +
                expandByte[m * 6 + 2] * 2 * 2 +
                expandByte[m * 6 + 3] * 2 +
                expandByte[m * 6 + 4]
        when (m) {
            0 -> getBoxBinary(s1[i][j])
            1 -> getBoxBinary(s2[i][j])
            2 -> getBoxBinary(s3[i][j])
            3 -> getBoxBinary(s4[i][j])
            4 -> getBoxBinary(s5[i][j])
            5 -> getBoxBinary(s6[i][j])
            6 -> getBoxBinary(s7[i][j])
            7 -> getBoxBinary(s8[i][j])
            else -> ""
        }.let {
            sBoxByte[m * 4] = it.substring(0, 1).toByte()
            sBoxByte[m * 4 + 1] = it.substring(1, 2).toByte()
            sBoxByte[m * 4 + 2] = it.substring(2, 3).toByte()
            sBoxByte[m * 4 + 3] = it.substring(3, 4).toByte()
        }
    }
    return sBoxByte
}

private fun pPermute(sBoxByte: ByteArray): ByteArray {
    return byteArrayOf(
        sBoxByte[15],
        sBoxByte[6],
        sBoxByte[19],
        sBoxByte[20],
        sBoxByte[28],
        sBoxByte[11],
        sBoxByte[27],
        sBoxByte[16],
        sBoxByte[0],
        sBoxByte[14],
        sBoxByte[22],
        sBoxByte[25],
        sBoxByte[4],
        sBoxByte[17],
        sBoxByte[30],
        sBoxByte[9],
        sBoxByte[1],
        sBoxByte[7],
        sBoxByte[23],
        sBoxByte[13],
        sBoxByte[31],
        sBoxByte[26],
        sBoxByte[2],
        sBoxByte[8],
        sBoxByte[18],
        sBoxByte[12],
        sBoxByte[29],
        sBoxByte[5],
        sBoxByte[21],
        sBoxByte[10],
        sBoxByte[3],
        sBoxByte[24]
    )
}

private fun finallyPermute(endByte: ByteArray): ByteArray {
    return byteArrayOf(
        endByte[39],
        endByte[7],
        endByte[47],
        endByte[15],
        endByte[55],
        endByte[23],
        endByte[63],
        endByte[31],
        endByte[38],
        endByte[6],
        endByte[46],
        endByte[14],
        endByte[54],
        endByte[22],
        endByte[62],
        endByte[30],
        endByte[37],
        endByte[5],
        endByte[45],
        endByte[13],
        endByte[53],
        endByte[21],
        endByte[61],
        endByte[29],
        endByte[36],
        endByte[4],
        endByte[44],
        endByte[12],
        endByte[52],
        endByte[20],
        endByte[60],
        endByte[28],
        endByte[35],
        endByte[3],
        endByte[43],
        endByte[11],
        endByte[51],
        endByte[19],
        endByte[59],
        endByte[27],
        endByte[34],
        endByte[2],
        endByte[42],
        endByte[10],
        endByte[50],
        endByte[18],
        endByte[58],
        endByte[26],
        endByte[33],
        endByte[1],
        endByte[41],
        endByte[9],
        endByte[49],
        endByte[17],
        endByte[57],
        endByte[25],
        endByte[32],
        endByte[0],
        endByte[40],
        endByte[8],
        endByte[48],
        endByte[16],
        endByte[56],
        endByte[24]
    )
}

private fun strToBt(str: String): ByteArray {
    val bt = ByteArray(64)
    if (str.length < 4) {
        for (i in str.indices) {
            val k = str[i]
            for (j in 0 until 16) {
                var pow = 1
                for (m in 15 downTo j + 1) {
                    pow *= 2
                }
                bt[16 * i + j] = (k.code / pow % 2).toByte()
            }
        }
        for (p in str.length until 4) {
            for (q in 0 until 16) {
                bt[16 * p + q] = 0.toByte()
            }
        }
    } else {
        for (i in 0 until 4) {
            val k = str[i]
            for (j in 0 until 16) {
                var pow = 1
                for (m in 15 downTo j + 1) {
                    pow *= 2
                }
                bt[16 * i + j] = (k.code / pow % 2).toByte()
            }
        }
    }
    return bt
}

private fun bt64ToHex(byteData: ByteArray): String {
    return buildString {
        for (i in 0 until 16) {
            buildString {
                for (j in 0 until 4) {
                    append(byteData[i * 4 + j])
                }
            }.also {
                append(it.toInt(2).toString(16).uppercase())
            }
        }
    }
}
private fun getKeyBytes(key: String): Array<ByteArray?> {
    val iterator = key.length / 4
    val keyBytes = arrayOfNulls<ByteArray>(iterator + 1)
    for (i in 0 until iterator) {
        keyBytes[i] = strToBt(key.substring(i * 4, i * 4 + 4))
    }
    if (key.length % 4 > 0) {
        keyBytes[iterator] = strToBt(key.substring(iterator * 4, key.length))
    }
    return keyBytes
}

private fun enc(dataByte: ByteArray, keyByte: ByteArray): ByteArray {
    val keys = generateKeys(keyByte)
    val ipByte = initPermute(dataByte)
    val ipLeft = ByteArray(32)
    val ipRight = ByteArray(32)
    val tempLeft = ByteArray(32)
    for (k in 0 until 32) {
        ipLeft[k] = ipByte[k]
        ipRight[k] = ipByte[32 + k]
    }
    for (i in 0 until 16) {
        for (j in 0 until 32) {
            tempLeft[j] = ipLeft[j]
            ipLeft[j] = ipRight[j]
        }
        val key = ByteArray(48)
        keys[i].copyInto(key)
        val tempRight = xor(pPermute(sBoxPermute(xor(expandPermute(ipRight), key))), tempLeft)
        tempRight.copyInto(ipRight)
    }
    val finalData = ByteArray(64)
    for (i in 0 until 32) {
        finalData[i] = ipRight[i]
        finalData[32 + i] = ipLeft[i]
    }
    return finallyPermute(finalData)
}

fun strEnc(data: String, firstKey: String?, secondKey: String?, thirdKey: String?): String {
    var encData = StringBuilder()
    var firstKeyBt = arrayOfNulls<ByteArray>(0)
    var secondKeyBt = arrayOfNulls<ByteArray>(0)
    var thirdKeyBt = arrayOfNulls<ByteArray>(0)
    var firstLength = 0
    var secondLength = 0
    var thirdLength = 0
    if (!firstKey.isNullOrEmpty()) {
        firstKeyBt = getKeyBytes(firstKey)
        firstLength = firstKeyBt.size
    }
    if (!secondKey.isNullOrEmpty()) {
        secondKeyBt = getKeyBytes(secondKey)
        secondLength = secondKeyBt.size
    }
    if (!thirdKey.isNullOrEmpty()) {
        thirdKeyBt = getKeyBytes(thirdKey)
        thirdLength = thirdKeyBt.size
    }

    if (data.isNotEmpty()) {
        if (data.length < 4) {
            val bt = strToBt(data)
            var encByte = ByteArray(0)
            if (!firstKey.isNullOrEmpty()) {
                if (!secondKey.isNullOrEmpty()) {
                    if (!thirdKey.isNullOrEmpty()) {
                        var tempBt = bt
                        for (x in 0 until firstLength) {
                            tempBt = enc(tempBt, firstKeyBt[x]!!)
                        }
                        for (y in 0 until secondLength) {
                            tempBt = enc(tempBt, secondKeyBt[y]!!)
                        }
                        for (z in 0 until thirdLength) {
                            tempBt = enc(tempBt, thirdKeyBt[z]!!)
                        }
                        encByte = tempBt
                    } else {
                        var tempBt = bt
                        for (x in 0 until firstLength) {
                            tempBt = enc(tempBt, firstKeyBt[x]!!)
                        }
                        for (y in 0 until secondLength) {
                            tempBt = enc(tempBt, secondKeyBt[y]!!)
                        }
                        encByte = tempBt
                    }
                } else {
                    var tempBt = bt
                    for (x in 0 until firstLength) {
                        tempBt = enc(tempBt, firstKeyBt[x]!!)
                    }
                    encByte = tempBt
                }
            }
            encData = StringBuilder(bt64ToHex(encByte))
        } else {
            val iterator = data.length / 4
            val remainder = data.length % 4
            for (i in 0 until iterator) {
                val tempData = data.substring(i * 4, i * 4 + 4)
                val tempByte = strToBt(tempData)
                var encByte = ByteArray(0)
                if (!firstKey.isNullOrEmpty()) {
                    if (!secondKey.isNullOrEmpty()) {
                        if (!thirdKey.isNullOrEmpty()) {
                            var tempBt = tempByte
                            for (x in 0 until firstLength) {
                                tempBt = enc(tempBt, firstKeyBt[x]!!)
                            }
                            for (y in 0 until secondLength) {
                                tempBt = enc(tempBt, secondKeyBt[y]!!)
                            }
                            for (z in 0 until thirdLength) {
                                tempBt = enc(tempBt, thirdKeyBt[z]!!)
                            }
                            encByte = tempBt
                        } else {
                            var tempBt = tempByte
                            for (x in 0 until firstLength) {
                                tempBt = enc(tempBt, firstKeyBt[x]!!)
                            }
                            for (y in 0 until secondLength) {
                                tempBt = enc(tempBt, secondKeyBt[y]!!)
                            }
                            encByte = tempBt
                        }
                    } else {
                        var tempBt = tempByte
                        for (x in 0 until firstLength) {
                            tempBt = enc(tempBt, firstKeyBt[x]!!)
                        }
                        encByte = tempBt
                    }
                }
                encData.append(bt64ToHex(encByte))
            }
            if (remainder > 0) {
                val remainderData = data.substring(iterator * 4, data.length)
                val tempByte = strToBt(remainderData)
                var encByte = ByteArray(0)
                if (!firstKey.isNullOrEmpty()) {
                    if (!secondKey.isNullOrEmpty()) {
                        if (!thirdKey.isNullOrEmpty()) {
                            var tempBt = tempByte
                            for (x in 0 until firstLength) {
                                tempBt = enc(tempBt, firstKeyBt[x]!!)
                            }
                            for (y in 0 until secondLength) {
                                tempBt = enc(tempBt, secondKeyBt[y]!!)
                            }
                            for (z in 0 until thirdLength) {
                                tempBt = enc(tempBt, thirdKeyBt[z]!!)
                            }
                            encByte = tempBt
                        } else {
                            var tempBt = tempByte
                            for (x in 0 until firstLength) {
                                tempBt = enc(tempBt, firstKeyBt[x]!!)
                            }
                            for (y in 0 until secondLength) {
                                tempBt = enc(tempBt, secondKeyBt[y]!!)
                            }
                            encByte = tempBt
                        }
                    } else {
                        var tempBt = tempByte
                        for (x in 0 until firstLength) {
                            tempBt = enc(tempBt, firstKeyBt[x]!!)
                        }
                        encByte = tempBt
                    }
                }
                encData.append(bt64ToHex(encByte))
            }
        }
    }
    return encData.toString()
}