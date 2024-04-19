package br.edu.puccampinas.pi3_es_2024_time_25

class CreditCard(
                var cid: String?,
                var cardNumber: Int,
                var titular: String,
                var cardExpiration: String,
                var cvv: Int) {

    class Validator(private val card: CreditCard) {

        fun isFormValid(): Boolean {
            return(isFormFilledOut() && areFieldsValid())
        }

        fun isFormFilledOut(): Boolean {
            return (card.titular.isNotEmpty() && card.cardNumber.toString().isNotEmpty() &&
                    card.cardExpiration.isNotEmpty() && card.cvv.toString().isNotEmpty())
        }

        fun areFieldsValid(): Boolean {
            return(getRealLength(card.cardNumber.toString()) == 16 && getRealLength(card.cardExpiration) == 4 &&
                    getRealLength(card.cvv.toString()) == 3)
        }

        private fun getRealLength(field: String): Int {
            var realLength = 0
            for (char in field) {
                if (char !in listOf('_', '/', '(', ')', '.', '-', ' ')) realLength++
            }
            return realLength
        }

       fun warnUser(): String {
            return when {
                !isFormFilledOut() -> "Preencha todos os campos."
                getRealLength(card.cardNumber.toString()) < 16 -> "Número do cartão inválido."
                getRealLength(card.cardExpiration) < 4 -> "Data de expiração inválida."
                else -> "Código de verificação inválido."
            }

       }
    }
}