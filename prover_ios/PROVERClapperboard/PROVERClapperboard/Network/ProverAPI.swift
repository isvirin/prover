import Foundation
import Moya

enum ProverAPI {
  case hello(hex: String)
  case submit(hex: String)
  case check(txhash: String)
}

extension ProverAPI: TargetType {

  var baseURL: URL {

    guard let url = URL(string: "http://mvp.prover.io/cgi-bin") else {
      fatalError("Can't create base url")
    }

    return url
  }

  var path: String {

    switch self {
    case .hello:
      return "/hello"
    case .submit, .check:
      return "/submit-message"
    }
  }

  var method: Moya.Method {

    switch self {
    case .hello, .submit, .check:
      return .post
    }
  }

  var sampleData: Data {
    return Data()
  }

  var task: Task {

    switch self {
    case let .hello(hex):
      return .requestParameters(parameters: ["user": hex], encoding: URLEncoding.default)
    case let .submit(hex):
      return .requestParameters(parameters: ["hex": hex], encoding: URLEncoding.default)
    case let .check(txhash):
      return .requestParameters(parameters: ["txhash": txhash], encoding: URLEncoding.default)
    }
  }

  var headers: [String: String]? {

    switch self {
    case .hello, .submit, .check:
      return ["Content-Type": "application/x-www-form-urlencoded",
              "Accept": "application/json"]
    }
  }
}
