import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private enum Gender {
        Female,
        Male,
    }
    private static class PersonTemplate {
        String firstName = null; // done
        String familyName = null; // done
        String motherName = null; // done
        String fatherName = null; // done
        String wifeName = null; // done
        Integer wifeId = null; // done
        String husbandName = null; // done
        Integer husbandId = null; // done
        String spouceName = null; // done
        List<String> brotherNames = new ArrayList<>(); // done
        List<String> sisterNames = new ArrayList<>(); // done
        List<Integer> siblingIds = new ArrayList<>(); // done
        List<Integer> sonIds = new ArrayList<>(); // done
        List<Integer> daughterIds = new ArrayList<>(); // done
        List<String> childrenNames = new ArrayList<>(); // done
        List<String> parentNames = new ArrayList<>(); // done
        List<Integer> parentIds = new ArrayList<>(); // done
        Integer siblingsNum = null;
        Integer childrenNum = null;
        Integer id = null; // done
        Gender gender = null; // done

        @Override
        public String toString() {
            return "PersonTemplate{" +
                    "firstName='" + firstName + '\'' +
                    ", familyName='" + familyName + '\'' +
                    ", motherName='" + motherName + '\'' +
                    ", fatherName='" + fatherName + '\'' +
                    ", wifeName='" + wifeName + '\'' +
                    ", wifeId=" + wifeId +
                    ", husbandName='" + husbandName + '\'' +
                    ", husbandId=" + husbandId +
                    ", spouceName='" + spouceName + '\'' +
                    ", brotherNames=" + brotherNames +
                    ", sisterNames=" + sisterNames +
                    ", siblingIds=" + siblingIds +
                    ", sonIds=" + sonIds +
                    ", daughterIds=" + daughterIds +
                    ", childrenNames=" + childrenNames +
                    ", parentNames=" + parentNames +
                    ", parentIds=" + parentIds +
                    ", siblingsNum=" + siblingsNum +
                    ", childrenNum=" + childrenNum +
                    ", id=" + id +
                    ", gender=" + gender +
                    '}';
        }
    }

    private static class Person {
        String firstName = null;
        String familyName = null;
        Gender gender = null;
        Integer[] parents = {null, null};
        Integer spouce = null;
        List<Integer> siblings = new ArrayList<>();
        List<Integer> children = new ArrayList<>();

        @Override
        public String toString() {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", familyName='" + familyName + '\'' +
                    ", gender=" + gender +
                    ", parents=" + Arrays.toString(parents) +
                    ", spouce=" + spouce +
                    ", siblings=" + siblings +
                    ", children=" + children +
                    '}';
        }
    }

    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        var path = "people.xml";
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(path));

        int peopleCount = 0;
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("people")) {
                    peopleCount = Integer.parseInt(startElement.getAttributeByName(new QName("count")).getValue());
                    break;
                }
            }
        }
        var people = new ArrayList<PersonTemplate>();
        var params = new HashSet<String>();
        while (true) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndDocument() || event.isEndElement())
                break;
            if (event.isCharacters())
                continue;
            assert event.isStartElement();
            StartElement start = event.asStartElement();
            var person = new PersonTemplate();
            var attribIter = start.getAttributes();
            while (attribIter.hasNext()) {
                var attrib = attribIter.next();
                switch (attrib.getName().getLocalPart()) {
                    case "id":
                        person.id = Integer.parseInt(attrib.getValue().substring(1));
                        break;
                    case "name":
                        var fullname = attrib.getValue();
                        assert fullname.trim().split(" +").length == 2 : Arrays.stream(fullname.trim().split(" +")).toList();
                        person.firstName = fullname.trim().split(" +")[0].trim();
                        person.familyName = fullname.trim().split(" +")[1].trim();
                        break;
                    default:
                        assert false;
                }
            }
            while (reader.hasNext()) {
                XMLEvent childEvent = reader.nextEvent();
                if (childEvent.isCharacters())
                    continue;
                if (childEvent.isEndElement()) {
                    if (childEvent.asEndElement().getName().getLocalPart().equals("person"))
                        break;
                    continue;
                }
                assert childEvent.isStartElement();
                StartElement localStart = childEvent.asStartElement();
                switch (localStart.getName().getLocalPart()) {
                    case "wife":
                        var nameOrId = parseOnlyValueAttrib(reader, localStart);
                        try {
                            person.wifeId = Integer.parseInt(nameOrId.substring(1));;
                        } catch (Exception e) {
                            if (!nameOrId.equals("UNKNOWN")) {
                                person.wifeName = nameOrId.trim().replaceAll(" +", " ");
                            }
                        }
                        break;
                    case "husband":
                        var nameOrId2 = parseOnlyValueAttrib(reader, localStart);
                        try {
                            person.husbandId = Integer.parseInt(nameOrId2.substring(1));;
                        } catch (Exception e) {
                            if (!nameOrId2.equals("UNKNOWN")) {
                                person.husbandName = nameOrId2.trim().replaceAll(" +", " ");
                            }
                        }
                        break;
                    case "mother":
                        person.motherName = parseOnlyInner(reader, localStart).trim().replaceAll(" +", " ");
                        assert person.motherName.trim().split(" +").length == 2;
                        break;
                    case "father":
                        person.fatherName = parseOnlyInner(reader, localStart).trim().replaceAll(" +", " ");
                        assert person.fatherName.trim().split(" +").length == 2;
                        break;
                    case "brother":
                        person.brotherNames.add(parseOnlyInner(reader, localStart).trim().replaceAll(" +", " "));
                        break;
                    case "sister":
                        person.sisterNames.add(parseOnlyInner(reader, localStart).trim().replaceAll(" +", " "));
                        break;
                    case "son":
                        person.sonIds.add(Integer.parseInt(parseOnlyValueAttrib(reader, localStart, "id").substring(1)));
                        break;
                    case "daughter":
                        person.daughterIds.add(Integer.parseInt(parseOnlyValueAttrib(reader, localStart, "id").substring(1)));
                        break;
                    case "children-number":
                        person.childrenNum = Integer.parseInt(parseOnlyValueAttrib(reader, localStart));
                        break;
                    case "siblings-number":
                        person.siblingsNum = Integer.parseInt(parseOnlyValueAttrib(reader, localStart));
                        break;
                    case "id":
                        person.id = Integer.parseInt(parseOnlyValueAttrib(reader, localStart).substring(1));
                        break;
                    case "firstname":
                        person.firstName = parseValueOrInner(reader, localStart).trim().replaceAll(" +", " ");
                        break;
                    case "first":
                        person.firstName = parseOnlyInner(reader, localStart).trim().replaceAll(" +", " ");
                        break;
                    case "family":
                    case "family-name":
                        person.familyName = parseOnlyInner(reader, localStart).trim();
                        break;
                    case "surname":
                        person.familyName = parseOnlyValueAttrib(reader, localStart).trim();
                        break;
                    case "gender":
                        var str = parseValueOrInner(reader, localStart);
                        person.gender = str.equals("F") || str.equals("female") ? Gender.Female : Gender.Male;
                        break;
                    case "child":
                        person.childrenNames.add(parseOnlyInner(reader, localStart).trim().replaceAll(" +", " "));
                        break;
                    case "parent":
                        var nameOrId3 = parseValueOrInner(reader, localStart);
                        try {
                            var id = Integer.parseInt(nameOrId3.substring(1));
                            person.parentIds.add(id);
                        } catch (Exception e) {
                            if (!nameOrId3.equals("UNKNOWN")) {
                                person.parentNames.add(nameOrId3.trim().replaceAll(" +", " "));
                            }
                        }
                        break;
                    case "spouce":
                        var name = parseMaybeValueAttrib(reader, localStart, "value");
                        if (name != null && !name.equals("NONE"))
                            person.spouceName = name.trim().replaceAll(" +", " ");
                        break;
                    case "siblings":
                        var siblings = parseMaybeValueAttrib(reader, localStart, "val");
                        if (siblings != null) {
                            for (var id : siblings.trim().split(" +")) {
                                person.siblingIds.add(Integer.parseInt(id.substring(1)));
                            }
                        }
                        break;
                    case "children":
                    case "fullname":
                        break;
                    default:
                        params.add(localStart.getName().getLocalPart());

                        // assert false;
                        break;
                }
            }
            people.add(person);
        }
        var peopleOut = new HashMap<Integer, Person>();
        var idsToTemps = new HashMap<Integer, ArrayList<PersonTemplate>>();
        var genderHints = new HashMap<Integer, ArrayList<Gender>>();
        var namesToTemps = new HashMap<String, ArrayList<PersonTemplate>>();
        for (var person : people) {
            if (person.id != null) {
                if (!peopleOut.containsKey(person.id)) {
                    peopleOut.put(person.id, new Person());
                    genderHints.put(person.id, new ArrayList<>());
                    idsToTemps.put(person.id, new ArrayList<>());
                }
                idsToTemps.get(person.id).add(person);
            } else {
                var key = person.firstName + " " + person.familyName;
                if (!namesToTemps.containsKey(key)) {
                    namesToTemps.put(key, new ArrayList<>());
                }
                namesToTemps.get(key).add(person);
            }
        }
        BiConsumer<Integer, Integer> setHusbandWife = (hId, wId) -> {
            if (!assertEquals(peopleOut.get(hId).spouce, wId))
                return;
            if (!assertEquals(peopleOut.get(wId).spouce, hId))
                return;
            peopleOut.get(hId).spouce = wId;
            peopleOut.get(wId).spouce = hId;
        };
        BiConsumer<Integer, Integer> addChild = (pId, cId) -> {
            var child = peopleOut.get(cId);
            if (child.parents[0] != null && child.parents[1] != null)
                if (!child.parents[0].equals(pId) && !child.parents[1].equals(pId))
                    return;
            if (!peopleOut.get(pId).children.contains(cId))
                peopleOut.get(pId).children.add(cId);
            if (child.parents[0] == null) {
                child.parents[0] = pId;
            } else if (!child.parents[0].equals(pId) && child.parents[1] == null) {
                child.parents[1] = pId;
            } else {
                assert child.parents[0].equals(pId) || child.parents[1].equals(pId);
            }
        };
        BiConsumer<Integer, Integer> addSibling = (lhsId, rhsId) -> {
            var lhs = peopleOut.get(lhsId);
            var rhs = peopleOut.get(rhsId);
            if (!lhs.siblings.contains(rhsId))
                lhs.siblings.add(rhsId);
            if (!rhs.siblings.contains(lhsId))
                rhs.siblings.add(lhsId);
            // Should all siblings have same parents?
            for (var parent : lhs.parents) {
                if (parent != null) {
                    addChild.accept(parent, rhsId);
                }
            }
            for (var parent : rhs.parents) {
                if (parent != null) {
                    addChild.accept(parent, lhsId);
                }
            }
        };
        for (var id : peopleOut.keySet()) {
            var temps = idsToTemps.get(id);
            Person person = peopleOut.get(id);
            for (var temp : temps) {
                assert temp.id != null;
                if (temp.firstName != null) {
                    assertEquals(person.firstName, temp.firstName);
                    person.firstName = temp.firstName;
                }
                if (temp.familyName != null) {
                    assertEquals(person.familyName, temp.familyName);
                    person.familyName = temp.familyName;
                }
                if (temp.wifeId != null) {
                    setHusbandWife.accept(id, temp.wifeId);
                    genderHints.get(temp.wifeId).add(Gender.Female);
                }
                if (temp.husbandId != null) {
                    setHusbandWife.accept(temp.husbandId, id);
                    genderHints.get(temp.husbandId).add(Gender.Male);
                }
                for (var sonId : temp.sonIds) {
                    addChild.accept(id, sonId);
                    genderHints.get(sonId).add(Gender.Male);
                }
                for (var daughterId : temp.daughterIds) {
                    addChild.accept(id, daughterId);
                    genderHints.get(daughterId).add(Gender.Female);
                }
                for (var parent : temp.parentIds) {
                    addChild.accept(parent, id);
                }
                for (var sibling : temp.siblingIds) {
                    addSibling.accept(id, sibling);
                }
            }
        }
        var namesToIds = new HashMap<String, ArrayList<Integer>>();
        for (var id : peopleOut.keySet()) {
            var person = peopleOut.get(id);
            assert (person.firstName != null) && (person.familyName != null);
            var key = person.firstName + " " + person.familyName;
            if (!namesToIds.containsKey(key)) {
                namesToIds.put(key, new ArrayList<>());
            }
            namesToIds.get(key).add(id);
        }
        for (var id : peopleOut.keySet()) {
            var temps = idsToTemps.get(id);
            var person = peopleOut.get(id);
            var additionals = namesToTemps.get(person.firstName + " " + person.familyName);
            if (additionals != null)
                temps.addAll(additionals.stream().filter((t) ->  {
                    if (!(t.id == null || t.id.equals(id)))
                        return false;
                    if (person.spouce != null && !person.spouce.equals(t.husbandId))
                        return false;
                    if (person.spouce != null && !person.spouce.equals(t.wifeId))
                        return false;
                    return true;
                }).toList());
            for (var temp : temps) {
                if (temp.wifeName != null) {
                    var ids = namesToIds.get(temp.wifeName);
                    assert ids != null && !ids.isEmpty();
                    var wifeId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.spouce != null)
                            return lhs.spouce.equals(id) ? 1 : -1;
                        if (rhs.spouce != null)
                            return rhs.spouce.equals(id) ? -1 : 1;
                        assert false;
                        return lhsId;
                    }).get();
                    setHusbandWife.accept(id, wifeId);
                    genderHints.get(wifeId).add(Gender.Female);
                }
                if (temp.husbandName != null) {
                    var ids = namesToIds.get(temp.husbandName);
                    assert ids != null && !ids.isEmpty();
                    var husbandId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.spouce != null)
                            return lhs.spouce.equals(id) ? 1 : -1;
                        if (rhs.spouce != null)
                            return rhs.spouce.equals(id) ? -1 : 1;
                        assert false;
                        return lhsId;
                    }).get();
                    setHusbandWife.accept(husbandId, id);
                    genderHints.get(husbandId).add(Gender.Male);
                }
                if (temp.spouceName != null) {
                    var ids = namesToIds.get(temp.spouceName);
                    assert ids != null && !ids.isEmpty();
                    var spouceId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.spouce != null)
                            return lhs.spouce.equals(id) ? 1 : -1;
                        if (rhs.spouce != null)
                            return rhs.spouce.equals(id) ? -1 : 1;
                        for (var childId : peopleOut.get(id).children) {
                            if (lhs.children.contains(childId)) {
                                return 1;
                            }
                            if (rhs.children.contains(childId)) {
                                return -1;
                            }
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    setHusbandWife.accept(spouceId, id);
                }
                if (temp.fatherName != null) {
                    var ids = namesToIds.get(temp.fatherName);
                    assert ids != null && !ids.isEmpty();
                    var fatherId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.children.contains(id)) {
                            return 1;
                        }
                        if (rhs.children.contains(id)) {
                            return -1;
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    addChild.accept(fatherId, id);
                    genderHints.get(fatherId).add(Gender.Male);
                }
                if (temp.motherName != null) {
                    var ids = namesToIds.get(temp.motherName);
                    assert ids != null && !ids.isEmpty();
                    var motherId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.children.contains(id)) {
                            return 1;
                        }
                        if (rhs.children.contains(id)) {
                            return -1;
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    addChild.accept(motherId, id);
                    genderHints.get(motherId).add(Gender.Female);
                }
                for (var brotherName : temp.brotherNames) {
                    var ids = namesToIds.get(brotherName);
                    assert ids != null && !ids.isEmpty();
                    var self = peopleOut.get(id);
                    var brotherId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.siblings.contains(id)) {
                            return 1;
                        }
                        if (rhs.siblings.contains(id)) {
                            return -1;
                        }
                        for (int i = 0; i < 2; i++) {
                            if (self.parents[i] != null) {
                                if ((self.parents[i].equals(lhs.parents[0]) || self.parents[i].equals(lhs.parents[1])))
                                    return 1;
                            }
                        }
                        for (int i = 0; i < 2; i++) {
                            if (self.parents[i] != null) {
                                if ((self.parents[i].equals(rhs.parents[0]) || self.parents[i].equals(rhs.parents[1])))
                                    return -1;
                            }
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    addSibling.accept(id, brotherId);
                    genderHints.get(brotherId).add(Gender.Male);
                }
                for (var sisterName : temp.sisterNames) {
                    var ids = namesToIds.get(sisterName);
                    assert ids != null && !ids.isEmpty();
                    var self = peopleOut.get(id);
                    var sisterId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        for (int i = 0; i < 2; i++) {
                            if (self.parents[i] != null) {
                                if ((self.parents[i].equals(lhs.parents[0]) || self.parents[i].equals(lhs.parents[1])))
                                    return 1;
                            }
                        }
                        for (int i = 0; i < 2; i++) {
                            if (self.parents[i] != null) {
                                if ((self.parents[i].equals(rhs.parents[0]) || self.parents[i].equals(rhs.parents[1])))
                                    return -1;
                            }
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    addSibling.accept(id, sisterId);
                    genderHints.get(sisterId).add(Gender.Female);
                }
                for (var childName : temp.childrenNames) {
                    var ids = namesToIds.get(childName);
                    assert ids != null && !ids.isEmpty();
                    var childId = ids.stream().filter((filterId) -> {
                        var filterPerson = peopleOut.get(filterId);
                        if (filterPerson.parents[0] != null && filterPerson.parents[1] != null && !filterPerson.parents[0].equals(id) && !filterPerson.parents[1].equals(id))
                            return false;
                        return true;
                    }).toList();
                    if (childId.size() == 1) {
                        addChild.accept(id, childId.get(0));
                    }
                }
                for (var parentName : temp.parentNames) {
                    var ids = namesToIds.get(parentName);
                    assert ids != null && !ids.isEmpty();
                    var parentId = ids.stream().max((lhsId, rhsId) -> {
                        var lhs = peopleOut.get(lhsId);
                        var rhs = peopleOut.get(rhsId);
                        if (lhs.children.contains(id)) {
                            return 1;
                        }
                        if (rhs.children.contains(id)) {
                            return -1;
                        }
                        assert false;
                        return lhsId;
                    }).get();
                    addChild.accept(parentId, id);
                }
                if (temp.wifeId != null) {
                    setHusbandWife.accept(id, temp.wifeId);
                    genderHints.get(temp.wifeId).add(Gender.Female);
                }
                if (temp.husbandId != null) {
                    setHusbandWife.accept(temp.husbandId, id);
                    genderHints.get(temp.husbandId).add(Gender.Male);
                }
                for (var sonId : temp.sonIds) {
                    addChild.accept(id, sonId);
                    genderHints.get(sonId).add(Gender.Male);
                }
                for (var daughterId : temp.daughterIds) {
                    addChild.accept(id, daughterId);
                    genderHints.get(daughterId).add(Gender.Female);
                }
                for (var parent : temp.parentIds) {
                    addChild.accept(parent, id);
                }
                for (var sibling : temp.siblingIds) {
                    addSibling.accept(id, sibling);
                }
            }
        }

        for (var id : genderHints.keySet()) {
            var maleCnt = genderHints.get(id).stream().filter((g) -> g.equals(Gender.Male)).count();
            peopleOut.get(id).gender = maleCnt >= genderHints.get(id).size() / 2.0 ? Gender.Male : Gender.Female;
        }

        System.out.println(peopleOut.get(395953));
        System.out.println(peopleOut.get(395954));
    }
    static String parseOnlyValueAttrib(XMLEventReader reader, StartElement start) throws XMLStreamException {
        return parseOnlyValueAttrib(reader, start, "value");
    }

    static String parseOnlyValueAttrib(XMLEventReader reader, StartElement start, String attribName) throws XMLStreamException {
        String res = parseMaybeValueAttrib(reader, start, attribName);
        assert res != null;
        return res;
    }

    static String parseMaybeValueAttrib(XMLEventReader reader, StartElement start, String attribName) throws XMLStreamException {
        var localAttribIter = start.getAttributes();
        if (!localAttribIter.hasNext()) {
            return null;
        }
        var attrib = localAttribIter.next();
        assert attrib.getName().getLocalPart().equals(attribName);
        String res = attrib.getValue();
        assert !localAttribIter.hasNext();
        XMLEvent next = reader.nextEvent();
        assert next.isEndElement();
        return res;
    }

    static String parseOnlyInner(XMLEventReader reader, StartElement start) throws XMLStreamException {
        var localAttribIter = start.getAttributes();
        assert !localAttribIter.hasNext();
        XMLEvent next = reader.nextEvent();
        assert next.isCharacters();
        String res = next.asCharacters().getData();
        next = reader.nextEvent();
        assert next.isEndElement();
        return res;
    }

    static String parseValueOrInner(XMLEventReader reader, StartElement start) throws XMLStreamException {
        var localAttribIter = start.getAttributes();
        String res;
        if (localAttribIter.hasNext()) {
            var attrib = localAttribIter.next();
            assert attrib.getName().getLocalPart().equals("value");
            res = attrib.getValue();
            assert !localAttribIter.hasNext();
        } else {
            XMLEvent next = reader.nextEvent();
            assert next.isCharacters();
            res = next.asCharacters().getData();
        }
        XMLEvent next = reader.nextEvent();
        assert next.isEndElement();
        return res;
    }
    static boolean assertEquals(Object lhs, Object rhs) {
        return lhs == null || lhs.equals(rhs);
    }
}