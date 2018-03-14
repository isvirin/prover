//
//  WalletViewController.swift
//  PROVERClapperboard
//
//  Created by Mac Mini on 13/03/2018.
//  Copyright © 2018 Nordavind. All rights reserved.
//

import UIKit

class WalletViewController: UITableViewController {
  
  private let balanceTitleLabel: UILabel = {
    let label = UILabel()
    label.text = "Balance"
    label.textColor = .white
    label.font = UIFont.systemFont(ofSize: 11)
    label.translatesAutoresizingMaskIntoConstraints = false
    return label
  }()
  
  private let balanceLabel: UILabel = {
    let label = UILabel()
    label.text = "0.0 P"
    label.textColor = .white
    label.font = UIFont.systemFont(ofSize: 30)
    label.translatesAutoresizingMaskIntoConstraints = false
    return label
  }()
  
  @IBAction func cancelButtonAction(_ sender: UIBarButtonItem) {
    navigationController?.dismiss(animated: true, completion: nil)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    configureNavigationBar()
    configureNavigationTitle()
    
    // Set up tableview
    tableView.rowHeight = UITableViewAutomaticDimension
    tableView.estimatedRowHeight = 44
  }
  
  private func configureNavigationBar() {
    guard let navigationBar = navigationController?.navigationBar else { return }
    let size = CGSize(width: navigationBar.bounds.width,
                      height: navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height)
    let image = #imageLiteral(resourceName: "background").resizedImage(newSize: size)
    navigationBar.barTintColor = UIColor(patternImage: image)
    navigationBar.tintColor = .white
  }
  
  private func configureNavigationTitle() {
    
    guard let bar = navigationController?.navigationBar else { return }
    
    bar.addSubview(balanceTitleLabel)
    bar.addSubview(balanceLabel)
    
    balanceTitleLabel.topAnchor
      .constraint(equalTo: bar.topAnchor, constant: 10).isActive = true
    balanceTitleLabel.leftAnchor
      .constraint(equalTo: bar.leftAnchor, constant: 100).isActive = true
    balanceTitleLabel.rightAnchor
      .constraint(equalTo: bar.rightAnchor, constant: 100).isActive = true
    
    balanceLabel.topAnchor
      .constraint(equalTo: balanceTitleLabel.bottomAnchor, constant: 2).isActive = true
    balanceLabel.leftAnchor
      .constraint(equalTo: balanceTitleLabel.leftAnchor).isActive = true
    balanceLabel.rightAnchor
      .constraint(equalTo: balanceTitleLabel.rightAnchor).isActive = true
  }
}

// MARK: - UITableViewDelegate
extension WalletViewController {
  override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    return UITableViewAutomaticDimension
  }
}
