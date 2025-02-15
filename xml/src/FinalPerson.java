import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "person")
class FinalPerson {
    @XmlAttribute
    @XmlID
    String id;
    @XmlElement
    String firstName = null;
    @XmlElement
    String familyName = null;
    @XmlElement
    Gender gender = null;

    @XmlElement
    @XmlIDREF
    FinalPerson mother = null;

    @XmlElement
    @XmlIDREF
    FinalPerson father = null;

    @XmlElement
    @XmlIDREF
    FinalPerson wife = null;

    @XmlElement
    @XmlIDREF
    FinalPerson husband = null;

    @XmlElement(name = "brother")
    @XmlElementWrapper(name = "brothers")
    @XmlIDREF
    List<FinalPerson> brothers = new ArrayList<>();

    @XmlElement(name = "sister")
    @XmlElementWrapper(name = "sisters")
    @XmlIDREF
    List<FinalPerson> sisters = new ArrayList<>();


    @XmlElement(name = "son")
    @XmlElementWrapper(name = "sons")
    @XmlIDREF
    List<FinalPerson> sons = new ArrayList<>();

    @XmlElement(name = "daughter")
    @XmlElementWrapper(name = "daughters")
    @XmlIDREF
    List<FinalPerson> daughters = new ArrayList<>();
}