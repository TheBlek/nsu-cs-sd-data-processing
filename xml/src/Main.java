import com.sun.jdi.VoidType;
import io.vavr.Function3;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {
    private enum Gender {
        Female,
        Male,
    }
    private static class PersonTemplate {
        String firstName = null; // done
        String familyName = null; // done
        String motherName = null;
        String fatherName = null;
        String wifeName = null;
        Integer wifeId = null; // done
        String husbandName = null;
        Integer husbandId = null; // done
        String spouceName = null;
        List<String> brotherNames = new ArrayList<>();
        List<String> sisterNames = new ArrayList<>();
        List<Integer> siblingIds = new ArrayList<>();
        List<Integer> sonIds = new ArrayList<>(); // done
        List<Integer> daughterIds = new ArrayList<>(); // done
        List<String> childrenNames = new ArrayList<>();
        List<String> parentNames = new ArrayList<>();
        List<Integer> parentIds = new ArrayList<>(); // done
        Integer siblingsNum = null;
        Integer childrenNum = null;
        Integer id = null;
        Gender gender = null;

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
                                person.wifeName = nameOrId;
                            }
                        }
                        break;
                    case "husband":
                        var nameOrId2 = parseOnlyValueAttrib(reader, localStart);
                        try {
                            person.husbandId = Integer.parseInt(nameOrId2.substring(1));;
                        } catch (Exception e) {
                            if (!nameOrId2.equals("UNKNOWN")) {
                                person.husbandName = nameOrId2;
                            }
                        }
                        break;
                    case "mother":
                        person.motherName = parseOnlyInner(reader, localStart);
                        assert person.motherName.trim().split(" +").length == 2;
                        break;
                    case "father":
                        person.fatherName = parseOnlyInner(reader, localStart);
                        assert person.fatherName.trim().split(" +").length == 2;
                        break;
                    case "brother":
                        person.brotherNames.add(parseOnlyInner(reader, localStart));
                        break;
                    case "sister":
                        person.sisterNames.add(parseOnlyInner(reader, localStart));
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
                        person.firstName = parseValueOrInner(reader, localStart).trim();
                        break;
                    case "first":
                        person.firstName = parseOnlyInner(reader, localStart).trim();
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
                        person.childrenNames.add(parseOnlyInner(reader, localStart));
                        break;
                    case "parent":
                        var nameOrId3 = parseValueOrInner(reader, localStart);
                        try {
                            var id = Integer.parseInt(nameOrId3.substring(1));
                            person.parentIds.add(id);
                        } catch (Exception e) {
                            if (!nameOrId3.equals("UNKNOWN")) {
                                person.parentNames.add(nameOrId3);
                            }
                        }
                        break;
                    case "spouce":
                        var name = parseMaybeValueAttrib(reader, localStart, "value");
                        if (name != null && !name.equals("NONE"))
                            person.spouceName = name;
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
        System.out.println(peopleCount);
//        System.out.println(people);
//        System.out.println(people.size());
//        System.out.println(peopleCount);
        var namesToTemps = new HashMap<String, ArrayList<PersonTemplate>>();
        for (var person : people) {
//            assert (person.firstName != null) == (person.familyName != null);
//            if ((person.familyName == null) != (person.firstName == null)) {
//                System.out.println(person);
//                assert person.id != null;
//            }
//            System.out.println(person.firstName + "; " + person.familyName);
            if (person.firstName != null && person.familyName != null) {
                var key = person.firstName + " " + person.familyName;
                if (!namesToTemps.containsKey(key))
                    namesToTemps.put(key, new ArrayList<>());
                namesToTemps.get(person.firstName + " " + person.familyName).add(person);
            }
        }
//        for (var entries : namesToTemps.values()) {
//            Integer id = null;
//            for (var entry : entries) {
//                if (entry.id != null) {
//                    if (id != null && !id.equals(entry.id)) {
//                        System.out.println(entries);
//                    }
////                    assert (id == null) || id.equals(entry.id);
//                    id = entry.id;
//                }
//            }
//        }
        System.out.println(namesToTemps.size());
        var peopleOut = new HashMap<Integer, Person>();
        var idsToTemps = new HashMap<Integer, ArrayList<PersonTemplate>>();
        var genderHints = new HashMap<Integer, ArrayList<Gender>>();
        for (var person : people) {
            if (person.id != null) {
                if (!peopleOut.containsKey(person.id)) {
                    peopleOut.put(person.id, new Person());
                    genderHints.put(person.id, new ArrayList<>());
                    idsToTemps.put(person.id, new ArrayList<>());
                }
                idsToTemps.get(person.id).add(person);
            }
        }
        System.out.println(peopleOut.size());
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
                BiConsumer<Integer, Integer> setHusbandWife = (hId, wId) -> {
                    assertEquals(peopleOut.get(hId).spouce, wId);
                    assertEquals(peopleOut.get(wId).spouce, hId);
                    peopleOut.get(hId).spouce = wId;
                    peopleOut.get(wId).spouce = hId;
                    genderHints.get(wId).add(Gender.Female);
                    genderHints.get(hId).add(Gender.Male);
                };
                if (temp.wifeId != null) {
                    setHusbandWife.accept(id, temp.wifeId);
                }
                if (temp.husbandId != null) {
                    setHusbandWife.accept(temp.husbandId, id);
                }
                BiConsumer<Integer, Integer> addChild = (pId, cId) -> {
                    if (!peopleOut.get(pId).children.contains(cId))
                        peopleOut.get(pId).children.add(cId);
                    var child = peopleOut.get(cId);
                    if (child.parents[0] == null) {
                        child.parents[0] = pId;
                    } else if (!child.parents[0].equals(pId) && child.parents[1] == null) {
                        child.parents[1] = pId;
                    } else {
                        assert child.parents[0].equals(pId) || child.parents[1].equals(pId);
                    }
                };
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
                    var lhs = peopleOut.get(sibling);
                    var rhs = peopleOut.get(id);
                    if (!lhs.siblings.contains(id))
                        lhs.siblings.add(id);
                    if (!rhs.siblings.contains(sibling))
                        rhs.siblings.add(sibling);
                    // Should all siblings have same parents?
                    for (var parent : lhs.parents) {
                        if (parent != null) {
                            addChild.accept(parent, id);
                        }
                    }
                    for (var parent : rhs.parents) {
                        if (parent != null) {
                            addChild.accept(parent, sibling);
                        }
                    }
                }
            }
//            var maleCnt = temps.stream().filter((p) -> p.gender.equals(Gender.Male)).count();
//            var totalCnt = temps.stream().filter((p) -> p.gender.equals(Gender.Male)).count();
//            if (maleCnt != )
//            person.gender = maleCnt >= temps.size() / 2.0 ? Gender.Male : Gender.Female;
        }
//        for (var person : people) {
//            if (person.id != null && person.firstName != null) {
//                peopleOut.get(person.id).firstName = person.firstName;
//            }
//            if (person.id != null && person.familyName != null) {
//                peopleOut.get(person.id).familyName = person.familyName;
//            }
//            if (person.id != null && person.gender != null) {
//                genderHints.get(person.id).add(person.gender);
//            }
//            if (person.id != null && person.wifeId != null) {
//                peopleOut.get(person.id).wife = person.wifeId;
//                peopleOut.get(person.wifeId).husband = person.id;
//                genderHints.get(person.wifeId).add(Gender.Female);
//                genderHints.get(person.id).add(Gender.Male);
//            }
//            if (person.id != null && person.husbandId != null) {
//                peopleOut.get(person.id).husband = person.husbandId;
//                genderHints.get(person.husbandId).add(Gender.Male);
//            }
//            if (person.id != null && !person.sonIds.isEmpty()) {
//                peopleOut.get(person.id).sons = person.sonIds;
//                for (var id : person.sonIds) {
//                    genderHints.get(id).add(Gender.Male);
//                }
//            }
//            if (person.id != null && !person.daughterIds.isEmpty()) {
//                peopleOut.get(person.id).daughters = person.daughterIds;
//                for (var id : person.daughterIds) {
//                    genderHints.get(id).add(Gender.Female);
//                }
//            }
//        }

        for (var id : genderHints.keySet()) {
            var maleCnt = genderHints.get(id).stream().filter((g) -> g.equals(Gender.Male)).count();
            peopleOut.get(id).gender = maleCnt >= genderHints.get(id).size() / 2.0 ? Gender.Male : Gender.Female;
        }
        System.out.println(people.size());
        System.out.println(people.stream().filter((p) -> p.id != null).count());
        System.out.println(peopleOut.get(397288));
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
    static void assertEquals(Object lhs, Object rhs) {
        assert lhs == null || lhs.equals(rhs);
    }
}