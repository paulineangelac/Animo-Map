# Animo-Map
DLSU Campus Navigation and Community Mobile App for ITISMOB 

## Mapbox setup

The Map screen uses the Mapbox Maps SDK. Each developer needs a free **public** access token (starts with `pk.`) from https://account.mapbox.com/.
Add it to `local.properties` (git-ignored, never commit it), then rebuild:

```
MAPBOX_ACCESS_TOKEN=pk.your_token_here
```

Building coordinates in `CampusMapData` are currently estimates and should be replaced with surveyed values.
