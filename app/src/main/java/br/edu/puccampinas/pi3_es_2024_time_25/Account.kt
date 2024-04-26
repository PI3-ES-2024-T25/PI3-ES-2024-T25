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

    class Validator(private val account: Account) { // classe de validador. irá ser utilizada com uma instancia (conta)

        fun isFormOneValid(): Boolean { // testa se o formulario está preenchido e se os campos foram preenchidos corretamente
            return (isFormOneFilledOut() && hasFormOneValidFields())
        }

        private fun isFormOneFilledOut(): Boolean { // testa se todos os campos foram preenchidos
            return (account.nome.isNotEmpty() && account.cpf.isNotEmpty()
                    && account.nascimento.isNotEmpty() && account.fone.isNotEmpty())
        }

        private fun hasFormOneValidFields(): Boolean { // testa se os campos foram preenchidos corretamente
            return (getRealLength(account.cpf) == 11 && getRealLength(account.nascimento) == 8 && hasLegalAge() && getRealLength(
                account.fone
            ) == 11)
        }

        private fun getRealLength(field: String): Int { //calcula a qtde de caracteres da string sem o caracter de mask
            var realLength = 0
            for (char in field) {
                if (char !in listOf('_', '/', '(', ')', '.', '-', ' ')) realLength++
            }
            return realLength
        }

        private fun hasLegalAge(): Boolean { // verifica se o usuário é maior de idade ou nao
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


        fun isFormTwoValid(): Boolean { // testa se o formulario esta prenchido, se o tamanho da senha é válido e se a senha de confirmação é igual a senha digitada
            return (isFormTwoFilledOut() && areBothPasswordEqual() && isPassSizeValid())
        }

        private fun isFormTwoFilledOut(): Boolean { // testa se o formulario foi totalmente preenchido

            return (account.email.isNotEmpty() && account.senha.isNotEmpty() && account.confirmPassword != "")

        }


        private fun areBothPasswordEqual(): Boolean { // testa se as duas senhas digitadas sao iguais
            return (account.senha == account.confirmPassword)
        }


        private fun isPassSizeValid(): Boolean { // testa se a senha tem no minimo 8 caracteres
            return account.senha.length >= 8
        }


        fun warnUser(): String { // retorna uma msg amigavel para o usuario de acordo com cada condiçao
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


        fun exceptionHandler(e: Exception?): String { // retorna uma msg amigavel para o usuario de acordo com cada exceção
            return when (e) {
                is FirebaseAuthUserCollisionException -> "O e-mail inserido já está registrado."
                is FirebaseNetworkException -> return "Sem conexão com a Internet."
                else -> return "Ocorreu um erro inesperado. Contate o suporte."
            }
        }


    }
}

