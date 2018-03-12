import Foundation
import Moya
import Result

enum APIError: Error {
  case convertationError
  case networkError
  case submitError(SubmitRequestError)
  case chechTransactionReturnNilError
}

typealias APIInfoResult = Result<Info, APIError>
typealias APIStringResult = Result<String, APIError>

class APIService {
  
  let provider = MoyaProvider<ProverAPI>()
  
  // Get nonce, contractAddress, gasPrice, ethBalance from service
  func getInfo(hex: String, handler: @escaping (APIInfoResult) -> Void) {
    
    provider.request(.hello(hex: hex)) { (result) in
      
      switch result {
      case .success(let responce):
        guard let infoResult = try? JSONDecoder().decode(InfoReslut.self, from: responce.data) else {
          handler(.failure(.convertationError))
          return
        }
        guard let info = Info(from: infoResult) else {
          handler(.failure(.convertationError))
          return
        }
        handler(.success(info))
      case .failure:
        handler(.failure(.networkError))
      }
    }
  }
  
  // send transaction in hexadecimal form and get hash this transaction
  func submit(hex: String, handler: @escaping (APIStringResult) -> Void) {
    
    provider.request(.submit(hex: hex)) { result in
      
      switch result {
      case .success(let responce):
        guard let submitResult = try? JSONDecoder().decode(SubmitResult.self, from: responce.data) else {
          handler(.failure(.convertationError))
          return
        }
        switch submitResult {
        case .result(let text):
          handler(.success(text))
        case .error(let error):
          handler(.failure(.submitError(error)))
        }
      case .failure:
        handler(.failure(.networkError))
      }
    }
  }
  
  // check status of transaction
  func check(txhash: String, handler: @escaping (APIStringResult) -> Void) {
    
    provider.request(.check(txhash: txhash)) { result in
      switch result {
      case .success(let responce):
        guard let checkResult = try? JSONDecoder().decode(CheckResult.self, from: responce.data) else {
          print("Can't convert responce to checkResult")
          handler(.failure(.convertationError))
          return
        }
        guard let value = checkResult.result else {
          handler(.failure(.chechTransactionReturnNilError))
          return
        }
        handler(.success(value))
      case .failure:
        handler(.failure(.networkError))
      }
    }
  }
}
