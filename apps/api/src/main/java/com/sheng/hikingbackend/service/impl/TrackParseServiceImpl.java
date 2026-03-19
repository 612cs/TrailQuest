package com.sheng.hikingbackend.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.service.TrackParseService;
import com.sheng.hikingbackend.service.impl.support.TrackParseResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackParseServiceImpl implements TrackParseService {

    private static final BigDecimal EARTH_RADIUS_METERS = BigDecimal.valueOf(6371000D);
    private final ObjectMapper objectMapper;

    @Override
    public TrackParseResult parse(MediaFile mediaFile) {
        String extension = mediaFile.getExtension() == null ? "" : mediaFile.getExtension().trim().toLowerCase();

        try (InputStream inputStream = URI.create(mediaFile.getUrl()).toURL().openStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(inputStream);
            document.getDocumentElement().normalize();

            return switch (extension) {
                case "gpx" -> parseGpx(document);
                case "kml" -> parseKml(document);
                default -> throw BusinessException.badRequest("UNSUPPORTED_TRACK_FORMAT", "仅支持 GPX 或 KML 轨迹文件");
            };
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.badRequest("TRACK_PARSE_FAILED", "轨迹文件解析失败，请检查 GPX/KML 格式");
        }
    }

    private TrackParseResult parseGpx(Document document) {
        List<List<TrackPoint>> lines = new ArrayList<>();
        List<TrackPoint> waypoints = new ArrayList<>();

        NodeList waypointNodes = document.getElementsByTagNameNS("*", "wpt");
        for (int i = 0; i < waypointNodes.getLength(); i++) {
            Element waypoint = (Element) waypointNodes.item(i);
            TrackPoint point = readPoint(waypoint, true);
            if (point != null) {
                waypoints.add(point);
            }
        }

        NodeList segmentNodes = document.getElementsByTagNameNS("*", "trkseg");
        for (int i = 0; i < segmentNodes.getLength(); i++) {
            Element segment = (Element) segmentNodes.item(i);
            NodeList pointNodes = segment.getElementsByTagNameNS("*", "trkpt");
            List<TrackPoint> segmentPoints = new ArrayList<>();
            for (int j = 0; j < pointNodes.getLength(); j++) {
                TrackPoint point = readPoint((Element) pointNodes.item(j), true);
                if (point != null) {
                    segmentPoints.add(point);
                }
            }
            if (!segmentPoints.isEmpty()) {
                lines.add(segmentPoints);
            }
        }

        if (lines.isEmpty()) {
            NodeList routeNodes = document.getElementsByTagNameNS("*", "rte");
            for (int i = 0; i < routeNodes.getLength(); i++) {
                Element route = (Element) routeNodes.item(i);
                NodeList pointNodes = route.getElementsByTagNameNS("*", "rtept");
                List<TrackPoint> routePoints = new ArrayList<>();
                for (int j = 0; j < pointNodes.getLength(); j++) {
                    TrackPoint point = readPoint((Element) pointNodes.item(j), true);
                    if (point != null) {
                        routePoints.add(point);
                    }
                }
                if (!routePoints.isEmpty()) {
                    lines.add(routePoints);
                }
            }
        }

        return buildParseResult("gpx", lines, waypoints);
    }

    private TrackParseResult parseKml(Document document) {
        List<List<TrackPoint>> lines = new ArrayList<>();
        List<TrackPoint> waypoints = new ArrayList<>();

        NodeList lineStringNodes = document.getElementsByTagNameNS("*", "LineString");
        for (int i = 0; i < lineStringNodes.getLength(); i++) {
            Element lineString = (Element) lineStringNodes.item(i);
            String coordinatesText = findChildText(lineString, "coordinates");
            List<TrackPoint> points = parseCoordinateText(coordinatesText);
            if (!points.isEmpty()) {
                lines.add(points);
            }
        }

        NodeList pointNodes = document.getElementsByTagNameNS("*", "Point");
        for (int i = 0; i < pointNodes.getLength(); i++) {
            Element pointElement = (Element) pointNodes.item(i);
            String coordinatesText = findChildText(pointElement, "coordinates");
            List<TrackPoint> points = parseCoordinateText(coordinatesText);
            if (!points.isEmpty()) {
                waypoints.add(points.get(0));
            }
        }

        return buildParseResult("kml", lines, waypoints);
    }

    private TrackParseResult buildParseResult(String sourceFormat, List<List<TrackPoint>> lines, List<TrackPoint> waypoints) {
        if (lines.isEmpty()) {
            throw BusinessException.badRequest("EMPTY_TRACK", "轨迹文件中没有可用的轨迹线");
        }

        int trackPointsCount = 0;
        Bounds bounds = new Bounds();
        BigDecimal distanceMeters = BigDecimal.ZERO;
        BigDecimal elevationGainMeters = BigDecimal.ZERO;
        BigDecimal elevationLossMeters = BigDecimal.ZERO;
        Instant startTime = null;
        Instant endTime = null;
        TrackPoint startPoint = null;
        TrackPoint endPoint = null;

        for (List<TrackPoint> line : lines) {
            trackPointsCount += line.size();
            for (int i = 0; i < line.size(); i++) {
                TrackPoint current = line.get(i);
                bounds.include(current.lng, current.lat);

                if (startPoint == null) {
                    startPoint = current;
                }
                endPoint = current;

                if (current.time != null) {
                    if (startTime == null || current.time.isBefore(startTime)) {
                        startTime = current.time;
                    }
                    if (endTime == null || current.time.isAfter(endTime)) {
                        endTime = current.time;
                    }
                }

                if (i == 0) {
                    continue;
                }

                TrackPoint previous = line.get(i - 1);
                distanceMeters = distanceMeters.add(haversineDistance(previous, current));

                if (previous.ele != null && current.ele != null) {
                    BigDecimal delta = current.ele.subtract(previous.ele);
                    if (delta.signum() > 0) {
                        elevationGainMeters = elevationGainMeters.add(delta);
                    } else if (delta.signum() < 0) {
                        elevationLossMeters = elevationLossMeters.add(delta.abs());
                    }
                }
            }
        }

        ObjectNode geoJson = objectMapper.createObjectNode();
        geoJson.put("type", "FeatureCollection");
        ArrayNode features = geoJson.putArray("features");
        ObjectNode feature = features.addObject();
        feature.put("type", "Feature");
        ObjectNode geometry = feature.putObject("geometry");
        if (lines.size() == 1) {
            geometry.put("type", "LineString");
            geometry.set("coordinates", toCoordinatesArray(lines.get(0)));
        } else {
            geometry.put("type", "MultiLineString");
            ArrayNode multiCoordinates = geometry.putArray("coordinates");
            for (List<TrackPoint> line : lines) {
                multiCoordinates.add(toCoordinatesArray(line));
            }
        }
        feature.set("properties", objectMapper.createObjectNode());

        return TrackParseResult.builder()
                .sourceFormat(sourceFormat)
                .geoJson(geoJson.toString())
                .trackPointsCount(trackPointsCount)
                .waypointCount(waypoints.size())
                .startLng(scale(startPoint.lng))
                .startLat(scale(startPoint.lat))
                .endLng(scale(endPoint.lng))
                .endLat(scale(endPoint.lat))
                .bboxMinLng(scale(bounds.minLng))
                .bboxMinLat(scale(bounds.minLat))
                .bboxMaxLng(scale(bounds.maxLng))
                .bboxMaxLat(scale(bounds.maxLat))
                .distanceMeters(scale(distanceMeters))
                .elevationGainMeters(scale(elevationGainMeters))
                .elevationLossMeters(scale(elevationLossMeters))
                .durationSeconds(startTime != null && endTime != null && !endTime.isBefore(startTime)
                        ? endTime.getEpochSecond() - startTime.getEpochSecond()
                        : null)
                .build();
    }

    private ArrayNode toCoordinatesArray(List<TrackPoint> points) {
        ArrayNode coordinates = objectMapper.createArrayNode();
        for (TrackPoint point : points) {
            ArrayNode coordinate = coordinates.addArray();
            coordinate.add(point.lng);
            coordinate.add(point.lat);
            if (point.ele != null) {
                coordinate.add(point.ele);
            }
        }
        return coordinates;
    }

    private TrackPoint readPoint(Element pointElement, boolean readEleAndTime) {
        String lngValue = pointElement.getAttribute("lon");
        String latValue = pointElement.getAttribute("lat");
        if (!StringUtils.hasText(lngValue) || !StringUtils.hasText(latValue)) {
            return null;
        }

        BigDecimal lng = new BigDecimal(lngValue.trim());
        BigDecimal lat = new BigDecimal(latValue.trim());
        BigDecimal ele = null;
        Instant time = null;

        if (readEleAndTime) {
            String eleText = findChildText(pointElement, "ele");
            if (StringUtils.hasText(eleText)) {
                ele = new BigDecimal(eleText.trim());
            }

            String timeText = findChildText(pointElement, "time");
            if (StringUtils.hasText(timeText)) {
                try {
                    time = Instant.parse(timeText.trim());
                } catch (Exception ignored) {
                    time = null;
                }
            }
        }

        return new TrackPoint(lng, lat, ele, time);
    }

    private List<TrackPoint> parseCoordinateText(String coordinatesText) {
        List<TrackPoint> points = new ArrayList<>();
        if (!StringUtils.hasText(coordinatesText)) {
            return points;
        }

        String[] tokens = coordinatesText.trim().split("\\s+");
        for (String token : tokens) {
            String[] values = token.split(",");
            if (values.length < 2) {
                continue;
            }
            BigDecimal lng = new BigDecimal(values[0].trim());
            BigDecimal lat = new BigDecimal(values[1].trim());
            BigDecimal ele = null;
            if (values.length >= 3 && StringUtils.hasText(values[2])) {
                ele = new BigDecimal(values[2].trim());
            }
            points.add(new TrackPoint(lng, lat, ele, null));
        }
        return points;
    }

    private String findChildText(Element parent, String localName) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String nodeName = node.getLocalName() != null ? node.getLocalName() : node.getNodeName();
            if (localName.equals(nodeName) || nodeName.endsWith(":" + localName)) {
                return node.getTextContent();
            }
        }
        return null;
    }

    private BigDecimal haversineDistance(TrackPoint start, TrackPoint end) {
        double lat1 = Math.toRadians(start.lat.doubleValue());
        double lat2 = Math.toRadians(end.lat.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLng = Math.toRadians(end.lng.doubleValue() - start.lng.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS.multiply(BigDecimal.valueOf(c));
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private record TrackPoint(BigDecimal lng, BigDecimal lat, BigDecimal ele, Instant time) {
    }

    private static class Bounds {
        private BigDecimal minLng;
        private BigDecimal minLat;
        private BigDecimal maxLng;
        private BigDecimal maxLat;

        void include(BigDecimal lng, BigDecimal lat) {
            minLng = minLng == null || lng.compareTo(minLng) < 0 ? lng : minLng;
            minLat = minLat == null || lat.compareTo(minLat) < 0 ? lat : minLat;
            maxLng = maxLng == null || lng.compareTo(maxLng) > 0 ? lng : maxLng;
            maxLat = maxLat == null || lat.compareTo(maxLat) > 0 ? lat : maxLat;
        }
    }
}
