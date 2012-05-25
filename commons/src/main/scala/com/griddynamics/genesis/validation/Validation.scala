package com.griddynamics.genesis.validation

import com.griddynamics.genesis.api.RequestResult
import util.matching.Regex

trait Validation[C] {

    def validCreate(value: C, function: (C) => Any): RequestResult = {
        validOnCreation(value) {
            function
        }
    }

    def validUpdate(value: C, function: (C) => C): RequestResult = {
        validOnUpdate(value) { item =>
            function(item)
            RequestResult(isSuccess = true)
        }
    }

    def validOnCreation[B](value: C)(block: C => B): RequestResult = {
        validateCreation(value) match {
            case Some(rr) => rr
            case _ => {
                block(value)
                RequestResult(isSuccess = true)
            }
        }
    }

    def validOnUpdate(value: C)(block: C => RequestResult): RequestResult = {
        validateUpdate(value) match {
            case Some(rr) => rr
            case _ => {
                block(value) ++ RequestResult(isSuccess = true)
            }
        }
    }

    protected def validateUpdate(c: C): Option[RequestResult]

    protected def validateCreation(c: C): Option[RequestResult]

    protected def filterResults(xs: Seq[Option[RequestResult]]): Option[RequestResult] = {
        val results: Seq[RequestResult] = xs.flatten
        results.isEmpty match {
            case true => None
            case _ => Some(results.reduceLeft(_ ++ _))
        }
    }
}

object Validation {
    val usernamePattern = """^([a-zA-Z0-9@.-]{2,64})$""".r
    val projectNamePattern = """^([\p{L}0-9@.\-/_ ]{2,64})$""".r
    val namePattern = """^([\p{L} ]{2,128})$""".r
    val emailPattern = """^[\w][\w.-]+@([\w-]+\.)+[a-zA-Z]{2,5}$""".r

    val projectNameErrorMessage = "Invalid format. Use a combination of letters, numbers, " +
                                  "spaces and following symbols: @.-/_. Length must be from 2 to 64"
    val nameErrorMessage = "Invalid format. Use a combination of capital and lowercase letters and spaces. " +
                           "Length must be from 2 to 128"

    def mustMatch(fieldName: String, error : String = "Invalid format")(pattern: Regex)(value: String) = {
        value match {
            case pattern(s) => None
            case _ => Some(RequestResult(variablesErrors = Map(fieldName -> error), isSuccess = false))
        }
    }

    def mustMatchName(value: String, fieldName: String) : Option[RequestResult] = mustMatch(fieldName)(namePattern)(value)
    def mustMatchUserName(value: String, fieldName: String) : Option[RequestResult] = mustMatch(fieldName)(usernamePattern)(value)
    def mustMatchEmail(value: String, fieldName: String) : Option[RequestResult] = mustMatch(fieldName)(emailPattern)(value)

    def mustPresent(value: Option[_], fieldName: String, error : String = "Must be present") = {
        value match {
            case None => Some(RequestResult(variablesErrors = Map(fieldName -> error), isSuccess = false))
            case _ => None
        }
    }

    def notEmpty(value: String, fieldName: String, error : String = "Must be present") = {
        if (value == null || value.trim.length == 0) Some(RequestResult(variablesErrors = Map(fieldName -> error), isSuccess = false))
        else None
    }

    def must[C](value: C, errorMessage: String = "")(block: C => Boolean) = {
        block(value) match {
            case true => None
            case false => Some(RequestResult(isSuccess = false, compoundServiceErrors = Seq(errorMessage)))
        }
    }

    def mustExist[C](value: C, errorMessage: String = "Not found")(finder: C => Option[C]) = {
      finder(value) match {
        case None => Some(RequestResult(isSuccess = false, isNotFound = true, compoundServiceErrors = Seq(errorMessage)))
        case Some(_) => None
      }
    }

}