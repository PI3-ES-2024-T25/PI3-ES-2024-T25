package br.edu.puccampinas.pi3_es_2024_time_25

class CreditCard(
                var cardNumber: String,
                var titular: String,
                var cardExpiration: String,
                var cvv: String) {

    class Validator(private val card: CreditCard) { // clase de validador que sera usada com uma instancia (cartao de credito)

        fun isFormValid(): Boolean {// testa se o formulario é valido
            return(isFormFilledOut() && areFieldsValid())
        }

        private fun isFormFilledOut(): Boolean { // testa se todos os campos foram preenchidos
            return (card.titular.isNotEmpty() && card.cardNumber.isNotEmpty() &&
                    card.cardExpiration.isNotEmpty() && card.cvv.isNotEmpty())
        }

        private fun areFieldsValid(): Boolean { // testa se os campos foram preenchidos corretamente
            return(getRealLength(card.cardNumber) == 16 && getRealLength(card.cardExpiration) == 4 &&
                    getRealLength(card.cvv) == 3)
        }

        private fun getRealLength(field: String): Int { // funçao que descobre a lenght de uma string sem o caracter de mask
            var realLength = 0
            for (char in field) {
                if (char !in listOf('_', '/', '(', ')', '.', '-', ' ')) realLength++
            }
            return realLength
        }

       fun warnUser(): String { // funçao que exibe a respectiva msg ao usuario
            return when {
                !isFormFilledOut() -> "Preencha todos os campos."
                getRealLength(card.cardNumber) < 16 -> "Número do cartão inválido."
                getRealLength(card.cardExpiration) < 4 -> "Data de expiração inválida."
                else -> "Código de verificação inválido."
            }

       }
    }
}