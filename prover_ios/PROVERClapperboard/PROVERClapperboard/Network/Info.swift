import Foundation

struct Info: Decodable {

  let nonce: String?
  let contractAddress: String
  let gasPrice: String
  let ethBalance: String?

  enum CodingKeys: String, CodingKey {
    case nonce
    case contractAddress
    case gasPrice
    case ethBalance
  }
}
