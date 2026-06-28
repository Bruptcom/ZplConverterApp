package com.zplconverter.app.data.model

import com.google.gson.annotations.SerializedName

data class AddressResponse(
    @SerializedName("cep") val cep: String?,
    @SerializedName("logradouro") val logradouro: String?,
    @SerializedName("complemento") val complemento: String?,
    @SerializedName("bairro") val bairro: String?,
    @SerializedName("localidade") val localidade: String?,
    @SerializedName("uf") val uf: String?,
    @SerializedName("erro") val erro: Boolean?
)
