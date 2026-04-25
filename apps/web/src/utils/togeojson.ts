function getFirstTag(node: Element | Document, tag: string) {
  return node.getElementsByTagName(tag)[0]
}

function getNodeText(node: Element | undefined) {
  return node ? node.textContent?.trim() || '' : ''
}

export function gpx(doc: Document) {
  const tracks = Array.from(doc.getElementsByTagName('trk'))
  const routes = Array.from(doc.getElementsByTagName('rte'))
  const waypoints = Array.from(doc.getElementsByTagName('wpt'))

  const features: any[] = []

  // Parse Track
  tracks.forEach((trk) => {
    const name = getNodeText(getFirstTag(trk, 'name'))
    const type = getNodeText(getFirstTag(trk, 'type'))
    const segments = Array.from(trk.getElementsByTagName('trkseg'))

    const coordinates = segments.flatMap((seg) => {
      const pts = Array.from(seg.getElementsByTagName('trkpt'))
      return pts.map((pt) => {
        const lat = parseFloat(pt.getAttribute('lat') || '0')
        const lon = parseFloat(pt.getAttribute('lon') || '0')
        return [lon, lat]
      })
    })

    if (coordinates.length > 0) {
      features.push({
        type: 'Feature',
        geometry: {
          type: 'LineString',
          coordinates,
        },
        properties: {
          name,
          type,
        },
      })
    }
  })

  // Parse Route
  routes.forEach((rte) => {
    const name = getNodeText(getFirstTag(rte, 'name'))
    const pts = Array.from(rte.getElementsByTagName('rtept'))
    const coordinates = pts.map((pt) => {
      const lat = parseFloat(pt.getAttribute('lat') || '0')
      const lon = parseFloat(pt.getAttribute('lon') || '0')
      return [lon, lat]
    })

    if (coordinates.length > 0) {
      features.push({
        type: 'Feature',
        geometry: {
          type: 'LineString',
          coordinates,
        },
        properties: {
          name,
        },
      })
    }
  })

  // Parse Waypoint
  waypoints.forEach((wpt) => {
    const name = getNodeText(getFirstTag(wpt, 'name'))
    const sym = getNodeText(getFirstTag(wpt, 'sym'))
    const lat = parseFloat(wpt.getAttribute('lat') || '0')
    const lon = parseFloat(wpt.getAttribute('lon') || '0')

    features.push({
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [lon, lat],
      },
      properties: {
        name,
        sym,
      },
    })
  })

  return {
    type: 'FeatureCollection',
    features,
  }
}
