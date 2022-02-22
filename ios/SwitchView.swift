import UIKit
import MapKit

class SwitchView: UIView, MKMapViewDelegate {
  
  /*@objc var isOn: Bool = false  {
    didSet {
          button.backgroundColor = isOn ? .yellow : .black
          button.setTitle(String(describing: isOn ? "I am ON" : "I am OFF"), for: .normal)
     }
  }*/
  override init(frame: CGRect) {
    super.init(frame: frame)
    NotificationCenter.default.addObserver(self, selector: #selector(self.methodOfReceivedNotification(notification:)), name: Notification.Name("UpdateProgresstNotification"), object: nil)

   self.addSubview(AppleMap)
  self.addSubview(ProgressLabel)
  }
  required init?(coder aDecoder: NSCoder) {
    fatalError("init has not been implemented")
  }
  
  
  @objc func methodOfReceivedNotification(notification: Notification) {
   
    print(notification.userInfo)
    
    if let Progress = notification.userInfo?["Progress"] as? String
    {
      ProgressLabel.text = Progress
    }
    
    
  }
  var ProgressLabel: UILabel = {
    let progresslbl = UILabel()
    
    progresslbl.font = UIFont.systemFont(ofSize: 20)
    progresslbl.frame =  CGRect(x: 100, y: 0, width: 300, height: 40)
    progresslbl.textColor = .black
    progresslbl.textAlignment = .center
    return progresslbl
  }()
  
  var AppleMap: MKMapView = {
   let mapView = MKMapView()
      mapView.frame = CGRect(x: 0, y: 0, width: 414, height: 896)
      mapView.mapType = MKMapType.standard
   
      mapView.isZoomEnabled = true
     mapView.isScrollEnabled = true
   mapView.isUserInteractionEnabled = true
//  MBXMBTilesOverlay *mbtilesOverlay;
   //let obj = MBXMBTilesOverlay()
    let obj = ViewController()
    obj.loadData()
    let path = ""
    obj.downloadFile(mapView)
    
    let annotation = MKPointAnnotation()
    annotation.title = "London"
    annotation.coordinate = CLLocationCoordinate2D(latitude: CLLocationDegrees(17.695930),
                                                    longitude: CLLocationDegrees(146.099486))
    mapView.addAnnotation(annotation)

    
    
    //let indexPath = IndexPath(row: 8, section: 0)

   // obj.tapButton(indexPath)
    
    
   let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
    
    
   if let fileURL = documentsUrl?.appendingPathComponent("MBTILES_08.mbtiles")
    {
 //  var filePath = Bundle.main.url(forResource: "MBTILES_08", withExtension: "mbtiles")
     print("path:",path)
 // var mbtilesOverlay = MBXMBTilesOverlay()
     
  //   mbtilesOverlay = MBXMBTilesOverlay(mbTilesPath:path)
  // print("mbtilesOverlay:",mbtilesOverlay)
     
     // mbtilesOverlay = [[MBXMBTilesOverlay alloc] initWithMBTilesPath:mbtilesPath];
     // [mapView addOverlay:mbtilesOverlay];
 // mapView.addOverlay(mbtilesOverlay as! MKOverlay)
   }
      return mapView
  }()
  

  /*
  func addapple() {
    let mapView = MKMapView()
       mapView.frame = CGRect(x: 0, y: 0, width: 150, height: 250)
       mapView.mapType = MKMapType.standard
    mapView.delegate = self
       mapView.isZoomEnabled = true
      mapView.isScrollEnabled = true
    mapView.isUserInteractionEnabled = true
  //  mapView.backgroundColor = .red

    let obj = MBXMBTilesOverlay()
    
    var filePath = Bundle.main.url(forResource: "MBTILES_08", withExtension: "mbtiles")
    print("path:",filePath?.absoluteString)
    var mbtilesOverlay: MBXMBTilesOverlay?
    mbtilesOverlay = MBXMBTilesOverlay(mbTilesPath:filePath?.absoluteString)
    print("mbtilesOverlay:",mbtilesOverlay)
  mapView.addOverlay(mbtilesOverlay as! MKOverlay)
    self.addSubview(mapView)
  }
  
*/
  
  
  
  @objc func toggleSwitchStatus() {
   // isOn = !isOn as Bool
  }
}
