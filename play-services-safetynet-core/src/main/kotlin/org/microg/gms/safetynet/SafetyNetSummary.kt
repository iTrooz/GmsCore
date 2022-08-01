package org.microg.gms.safetynet

import android.graphics.Color
import android.util.Base64
import com.google.android.gms.common.api.Status
import org.json.JSONObject
import kotlin.properties.Delegates

data class SafetyNetSummary(
    val requestType: SafetyNetRequestType,


    // request data
    val packageName: String,
    val key: String,
    val nonce: ByteArray?, // null with SafetyNetRequestType::RECAPTCHA
    val timestamp: Long,

    ) {

    var id by Delegates.notNull<Int>()

    // result data
    // note : requestStatus do not represent the actual status in case of an attestation, it will be in resultData
    var resultStatus: Status? = null
    var resultData: String? = null


    fun getInfoMessage() : Pair<Int, String> {
        if(resultStatus==null)return Pair(Color.CYAN, "Not completed yet")

        if(requestType== SafetyNetRequestType.ATTESTATION){


            val (_, payload, _) = try {
                resultData!!.split(".")
            } catch (e: Exception) {
                return Pair(Color.RED, "Invalid JWS")
            }

            val (basicIntegrity, ctsProfileMatch) = try {
                JSONObject(Base64.decode(payload, Base64.URL_SAFE).decodeToString()).let {
                    Pair(
                        it.optBoolean("basicIntegrity", false),
                        it.optBoolean("ctsProfileMatch", false)
                    )
                }
            } catch (e: Exception) {
                return Pair(Color.RED, "Invalid JSON")
            }

            return when {
                basicIntegrity && ctsProfileMatch -> {
                    Pair(Color.GREEN, "Integrity and CTS passed")
                }
                basicIntegrity -> {
                    // 0xffa500 is orange
                    Pair(0xffa500, "CTS failed")
                }
                else -> {
                    Pair(Color.RED, "Integrity failed")
                }
            }
        }else{
            return if(resultStatus!!.isSuccess){
                Pair(Color.GREEN, "ReCaptcha passed")
            }else{
                Pair(Color.RED, resultStatus!!.statusMessage)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SafetyNetSummary

        if (requestType != other.requestType) return false
        if (packageName != other.packageName) return false
        if (key != other.key) return false
        if (nonce != other.nonce) return false
        if (resultStatus != other.resultStatus) return false
        if (resultData != other.resultData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestType.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + nonce.hashCode()
        result = 31 * result + (resultStatus?.hashCode() ?: 0)
        result = 31 * result + (resultData?.hashCode() ?: 0)
        return result
    }


}