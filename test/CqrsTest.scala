/**
 * Created by vim on 5/14/14.
 */
class CqrsTest {

  // 1. Create CellsGroup1, CellsGroup2, RootGroup
  // 2. Create AmountView for all cells ( binded to the RootGroup ), and AmountView for every CellGroup
  // 3. Create Cell11, Cell12, Cell13 and put it into CellsGroup1
  // 4. Create Cell21, Cell22, Cell23 and put it into CellsGroup2
  // 5. Create Cell3 and put it directly into RootGroup
  // 6. Update values in the cells and ensure that total views will be updated

  // When creating Cell's and Group's ensure that there will be created corresponding views
  // Ensure that all sequence is represented by commands and can be replayed.

  // Sequence for creating and attaching cell to the group:
  //  - PUT cell JSON data to the /nodes/group33/cells ( address by url ) or to the /groups/1/cells ( address by id )
  //  - As are result - both this calls needs to be resolved into CreateAndAttachCell command
  //  - CreateAndAttachCell command needs to be dispatched to the CmdDispatcher actor
  //  - CmdDispatcher actor will create CreateAndAttachCellExecutor actor and send command
  //  - CreateAndAttachCellExecutor will send Create message to the CellRepository actor
  //  - CellRepository actor will create persistent Cell actor and send it to the CreateAndAttachCellExecutor
  //  - CreateAndAttachCellExecutor will resolve Group by send Get message to the GroupsRepository actor
  //  - CreateAndAttachCellExecutor will send AttachCell command to the Group persistent actor

  // NOTE: SEE ALSO NOTES ABOUT EVENTS, MODEL AND VIEW

  //  ВОПРОС: ВСЕ ЛИ КОММАНДЫ ДОЛЖНЫ ИДТИ ЧЕРЕЗ CmdDispatcher
  //  ВОЗМОЖНО ЛИ ОРГАНИЧНО ОПИСАТЬ ДЕЙСТВИЯ ДЛЯ compound command ? IE: Create(..) | Get(..) | Attach(...)
  //  И ВЫПОЛНЯТЬ ИХ ВСЕ В РАМКАХ CmdDispatcher БЕЗ СОЗДАНИЯ ДОП. АКТОРОВ.

  Chain(RootGroup.Create(), Parallel(List(
    Chain(CellsGroup.create(), Parallel(List(
      Cell.create("cell11", 1),
      Cell.create("cell12", 2),
      Cell.create("cell13", 3)
    )),
    Chain(CellsGroup.create(), Parallel(List(
      Cell.create("cell21", 11),
      Cell.create("cell22", 12),
      Cell.create("cell23", 13)
    )),
    Cell.create("cell3", 30)
  )))

  // Ok, at this moment we will be able to retrieve views and check correctness of they data

  // After that will try to modify some data and ensure that views will be updated

  // After that we can try to render entire page and check results




}
