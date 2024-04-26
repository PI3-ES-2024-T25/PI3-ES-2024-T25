package br.edu.puccampinas.pi3_es_2024_time_25

import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class Account(
              val nome: String,
              val cpf: String,
              val nascimento: String,
              val fone: String,
              var email: String,
              var senha: String){

            var confirmPassword = ""

    class Validator(private val account: Account) {

        fun isFormOneValid(): Boolean {
            return (isFormOneFilledOut() && hasFormOneValidFields())
        }

        private fun isFormOneFilledOut(): Boolean {
            return (account.nome.isNotEmpty() && account.cpf.isNotEmpty()
                    && account.nascimento.isNotEmpty() && account.fone.isNotEmpty())
        }

        private fun hasFormOneValidFields(): Boolean {
            return (getRealLength(account.cpf) == 11 && getRealLength(account.nascimento) == 8 && hasLegalAge() && getRealLength(
                account.fone
            ) == 11)
        }

        private fun getRealLength(field: String): Int {
            var realLength = 0
            for (char in field) {
                if (char !in listOf('_', '/', '(', ')', '.', '-', ' ')) realLength++
            }
            return realLength
        }

        private fun hasLegalAge(): Boolean {
            try {

                val dataAtual = LocalDate.now()
                val nascimentoUserString = account.nascimento
                val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val nascimentoUser = LocalDate.parse(nascimentoUserString, formatoData)

                val idade = Period.between(nascimentoUser, dataAtual).years

                return idade >= 18
            }

            catch (e: DateTimeParseException) {
                println("Date could not be converted: $e")
                return false
            }
        }


        fun isFormTwoValid(): Boolean {
            return (isFormTwoFilledOut() && areBothPasswordEqual() && isPassSizeValid())
        }

        private fun isFormTwoFilledOut(): Boolean {

            return (account.email.isNotEmpty() && account.senha.isNotEmpty() && account.confirmPassword != "")

        }


        private fun areBothPasswordEqual(): Boolean {
            return (account.senha == account.confirmPassword)
        }


        private fun isPassSizeValid(): Boolean {
            return account.senha.length >= 8
        }


        fun warnUser(): String {
            return when {
                !isFormOneFilledOut()-> "Preencha todos os campos."
                getRealLength(account.cpf) < 11 -> "CPF inválido."
                getRealLength(account.nascimento) < 8 -> "Data de nascimento inválida."
                getRealLength(account.fone) < 11 -> "Telefone inválido."
                !hasLegalAge() -> "Você deve ter mais que 18 anos para criar uma conta."
                !isFormTwoFilledOut() -> "Preencha todos os campos."
                !isPassSizeValid() -> "A senha deve ter no mínimo 8 digitos"
                else -> "As senhas não correspondem."
            }
        }


        fun exceptionHandler(e: Exception?): String {
            return when (e) {
                is FirebaseAuthUserCollisionException -> "O e-mail inserido já está registrado."
                is FirebaseNetworkException -> return "Sem conexão com a Internet."
                else -> return "Ocorreu um erro inesperado. Contate o suporte."
            }
        }


    }
}

