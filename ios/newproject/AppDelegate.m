#import "AppDelegate.h"

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTRootView.h>

#ifdef FB_SONARKIT_ENABLED
#import <FlipperKit/FlipperClient.h>
#import <FlipperKitLayoutPlugin/FlipperKitLayoutPlugin.h>
#import <FlipperKitUserDefaultsPlugin/FKUserDefaultsPlugin.h>
#import <FlipperKitNetworkPlugin/FlipperKitNetworkPlugin.h>
#import <SKIOSNetworkPlugin/SKIOSNetworkAdapter.h>
#import <FlipperKitReactPlugin/FlipperKitReactPlugin.h>
#import "MBXMBTilesOverlay.h"
static void InitializeFlipper(UIApplication *application) {
  FlipperClient *client = [FlipperClient sharedClient];
  SKDescriptorMapper *layoutDescriptorMapper = [[SKDescriptorMapper alloc] initWithDefaults];
  [client addPlugin:[[FlipperKitLayoutPlugin alloc] initWithRootNode:application withDescriptorMapper:layoutDescriptorMapper]];
  [client addPlugin:[[FKUserDefaultsPlugin alloc] initWithSuiteName:nil]];
  [client addPlugin:[FlipperKitReactPlugin new]];
  [client addPlugin:[[FlipperKitNetworkPlugin alloc] initWithNetworkAdapter:[SKIOSNetworkAdapter new]]];
  [client start];
}
#endif

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
#ifdef FB_SONARKIT_ENABLED
  InitializeFlipper(application);
#endif
  [self getdetails];
  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
  RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:bridge
                                                   moduleName:@"newproject"
                                            initialProperties:nil];

  if (@available(iOS 13.0, *)) {
      rootView.backgroundColor = [UIColor systemBackgroundColor];
  } else {
      rootView.backgroundColor = [UIColor whiteColor];
  }

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  UIViewController *rootViewController = [UIViewController new];
  rootViewController.view = rootView;
  self.window.rootViewController = rootViewController;
  [self.window makeKeyAndVisible];
  return YES;
}

-(void)getdetails
{
     
  // Search through the documents directory for a path containing Converted.plist
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *plistPath = [documentsDirectory stringByAppendingPathComponent:@"Converted.plist"];
  
  // Examine Contents of the filesystem
  NSFileManager *fileManager = [NSFileManager defaultManager];
  
  // If the plist path exists then exec
  BOOL plistExists = [fileManager fileExistsAtPath:plistPath isDirectory:NO];
  
  if (plistExists) {
      NSLog(@"plist has already been created.");
      NSLog(@"Overwriting old mbtiles_catalog.json");
      
      // Else if the plist doesn't already exist
  } else if (!plistExists){
      NSLog(@"Downloading mbtiles_catalog.json file started...");
      NSLog(@"%@", plistPath);
      
      // Download mbtiles_catalog.json
      NSString *urlToDownload = @"https://tileservice.charts.noaa.gov/mbtiles/mbtiles_catalog.json";
      NSURL  *url = [NSURL URLWithString:urlToDownload];
      NSData *urlData = [NSData dataWithContentsOfURL:url];
      
      if ( urlData ) {
          NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
          NSString *documentsDirectory = [paths objectAtIndex:0];
          
          NSString *filePath = [NSString stringWithFormat:@"%@/%@", documentsDirectory,@"mbtiles_catalog.json"];
          
          [urlData writeToFile:filePath atomically:YES];
          NSLog(@"mbtiles_catalog.json file saved");
      }
      
      NSLog(@"Generating First pList");
      
      NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
      NSString *documentsPath = [paths objectAtIndex:0];
      
      // Mbtiles_catalog file path in documents directory
      NSString *filePath = [documentsPath stringByAppendingPathComponent:@"mbtiles_catalog.json"];
      
      // Fetch data from the mbtiles_catalog.json
      NSData *data = [NSData dataWithContentsOfFile:filePath];
      NSError* error;
      
      // Convert mbtiles_json into Mutable Dictionary
      NSMutableDictionary *dict = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
      
      // Write data to the plist filepath
      NSString *plistPath = [documentsPath stringByAppendingPathComponent:@"Converted.plist"];
      
      NSError *writeError = nil;
      NSData *plistData = [NSPropertyListSerialization dataWithPropertyList:dict format:NSPropertyListXMLFormat_v1_0 options:NSPropertyListImmutable error:&writeError];
      
      if (plistData) {
          [plistData writeToFile:plistPath atomically:YES];
          NSLog(@"Converted to plist");
      } else {
          NSLog(@"Error in saveData: %@", error);
      }
      
      // For some reason if the plist path doesnt already exist then create the Converted.plist path
      if (![fileManager fileExistsAtPath: plistPath]) {
          
          plistPath = [documentsDirectory stringByAppendingPathComponent: [NSString stringWithFormat:@"Converted.plist"] ];
      }
      
      // Create dictionary for storing data from mbtiles_catalog.json
      NSMutableDictionary *catalogData;
      
      if ([fileManager fileExistsAtPath: plistPath]) {
          
          catalogData = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
          
      } else {
          
          // If the file doesn’t exist, create an empty dictionary
          
          catalogData = [[NSMutableDictionary alloc] init];
      }
      
      NSMutableDictionary* quilted_tilesets = [catalogData objectForKey:@"quilted_tilesets"];
      
      NSLog(@"The following should contain a field status that is set to installed.");
      
      // Iterate through dictionary and set all statuses to Not Installed
      for (id key in [quilted_tilesets allKeys]) {
          id value = [quilted_tilesets objectForKey:key];
          if ([value isKindOfClass:[NSMutableDictionary class]]) {
              [value setObject:@"Not Installed" forKey:@"status"];
          }
      }
      [catalogData writeToFile:plistPath atomically:YES];
  }
}
- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

@end
