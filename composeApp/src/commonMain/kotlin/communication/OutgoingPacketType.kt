/** The type of packet sent by the client.
 * */
enum class OutgoingPacketType {

    /** The client is submitting a game code.
     * This packet contains:
     * - the game code encoded
     * */
    CodeSubmit,

    /** Requests information about the current board.
     * **This packet does not contain any information.**
     */
    RequestCurrentStatus,

    /** This packet is sent when the player attempts to make a move.
     * It contains:
     * - _4 bits_ which field the symbol is set on
     */
    PlayerMakeMove,

    /** Sent when the player wants to reset the board.
     * **This packet does not contain any information.**
     */
    BoardReset,

    /** Sent when the player wants to toggle their symbol
     * **This packet does not contain any information.**
     */
    ToggleSymbol,
}