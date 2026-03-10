export function gpx(doc: Document) {
    const get1 = (node: Element | Document, tag: string) => node.getElementsByTagName(tag)[0]
    const nodeVal = (node: Element | undefined) => (node ? node.textContent?.trim() || '' : '')

    const tracks = Array.from(doc.getElementsByTagName('trk'))
    const routes = Array.from(doc.getElementsByTagName('rte'))
    const waypoints = Array.from(doc.getElementsByTagName('wpt'))

    const features: any[] = []

    // Parse Track
    tracks.forEach((trk) => {
        const name = nodeVal(get1(trk, 'name'))
        const type = nodeVal(get1(trk, 'type'))
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
        const name = nodeVal(get1(rte, 'name'))
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
        const name = nodeVal(get1(wpt, 'name'))
        const sym = nodeVal(get1(wpt, 'sym'))
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
