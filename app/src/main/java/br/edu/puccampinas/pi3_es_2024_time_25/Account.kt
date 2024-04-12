package br.edu.puccampinas.pi3_es_2024_time_25

import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class Account(var uid: String?,
              var nome: String,
              val cpf: String,
              val nascimento: String,
              val fone: String,
              val email: String,
              var senha: String) : AppCompatActivity() {

    class Validator(val account: Account) {
        
        private fun isFormOneValid(): Boolean {
            return(isFormOneFilledOut() && hasFormOneValidFields())
        }
        private fun isFormOneFilledOut(): Boolean {
            return (account .nome.isNotEmpty() && account.cpf.isNotEmpty()
                    && account.nascimento.isNotEmpty() && account.fone.isNotEmpty())
        }

        private fun hasFormOneValidFields(): Boolean {
            return (getRealLength(account.cpf) == 11 && getRealLength(account.nascimento) == 8 && hasLegalAge() && getRealLength(account.fone) == 11)
        }

        private fun getRealLength(field: String): Int {
            var realLength = 0
            for(char in field) {
                if(char!= '_' && char!= '/' && char!= '(' && char != ')' && char!='.' && char!='-' && char!= ' ') realLength++
            }
            return realLength
        }

        private fun hasLegalAge(): Boolean {
            val dataAtual = LocalDate.now()
            val nascimentoUserString = account.nascimento
            val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val nascimentoUser = LocalDate.parse(nascimentoUserString, formatoData)

            val idade = Period.between(nascimentoUser, dataAtual).years

            return idade>=18
        }
        // ATUALMENTE, OS VALIDADORES DESTA CLASSE NÃO ESTÃO FUNCIONANDO, ESTÃO EM USO OS DAS RESPECTIVAS ACTIVITIES
        // OS DADOS ATUALMENTE ESTAO SENDO PASSADOS DA PRIMEIRA PARA A SEGUNDA ATRAVES DE UM VETOR
        // NECESSARIO REFATORAR PARA USAR ESTA CLASSE E PASSAR OS DADOS VIA GSON CONFORME AS INTRUÇÕES ABAIXO
        // TODO: implementar as funções abaixo para funcionarem com a classe (ajustes e eliminação de redundâncias)
//
//        private fun avisaUsuario(): String {
//            return when {
//                !preencheuCampos() -> "Preencha todos os campos."
//                !CPF.isDone -> "CPF inválido."
//                !dataNasc.isDone -> "Data de nascimento inválida."
//                !maiorIdade() -> "Você deve ter mais que 18 anos para criar uma conta."
//                else -> "Telefone inválido."
//            }
//        }
//
//
        // TODO: CONTINUAR A PARTIR DAQUI 
//        private fun isFormTwoFilledOut(): Boolean {
//            return (account.email.isNotEmpty() && senha.text.toString()
//                .isNotEmpty() && confirmaSenha.text.toString().isNotEmpty())
//
//        }
//
//        private fun tamanhoSenha(): Boolean {
//            return senha.text.toString().length >= 8
//        }
//


        private fun confereSenha(confirmaSenha: String): Boolean {
            return (account.senha == confirmaSenha)
        }
//    }
//
//    private fun avisaUsuario(): String {
//        return when {
//            !preencheuForm2() -> "Preencha todos os campos."
//            !confereSenha() -> "As senhas digitadas não são iguais"
//            else -> "A senha deve ter no mínimo 8 dígitos."
//        }
//    }
//
//    private fun trataExcecao(e: Exception?): String {
//        return when(e) {
//            is FirebaseAuthUserCollisionException -> "O e-mail inserido já foi registrado."
//            is FirebaseNetworkException -> return "Sem conexão com a Internet."
//            else -> return "Ocorreu um erro. Contate o suporte."
//        }
    }


}

