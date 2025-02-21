# Changelog

## 1.1.0 - 2025-02-21
[Requires Exposure 1.8.4+]
- Updated to 1.21.1
- Exporting now stores PNG on client's pc, instead of on the server.
- "Stop Export" button will now show when export is in progress.
- Slightly changed filtering in search:
  - Size is now matched with `=size:<size>` instead of `=x<size>`
  - Added `=bw` filter that matches black and white exposures. Same as `!=color` and vise-versa. Just for convenience.
  - Added `=palette:<palette>` filter that matches palette 🤯. 

## 1.0.3 - 2024-08-04
[Requires Exposure 1.7.5+]
- Added `=projected` filter to show only exposures loaded from file with Interplanar Projector.
- Fixed crash when clicking on an exposure in catalog.

## 1.0.2 - 2024-07-22
- [Forge] Fixed permission error when trying to join a dedicated server. 

## 1.0.1 - 2024-07-19
[Requires Exposure 1.7.3+]
- Fixed error when trying to export exposures on a dedicated server.
- Small improvements to the Catalog GUI.

## 1.0.0
- Release